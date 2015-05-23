package controllers.analytics

import com.google.inject._
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date
import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.analytics.definitions.AnalyticsService
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
import java.time._
import java.time.format.DateTimeFormatter

class AnalyticsController @Inject()(
  analyticsService: AnalyticsService
) extends Controller {

  private lazy val Total = 0
  private lazy val Detailed = 1

  private def validateDate(dateStr: String, endDate: Boolean = false): Try[Date] = {
    try {
      val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
      val ld = if(!endDate){
        LocalDate.parse(dateStr, formatter).atStartOfDay().atZone(ZoneId.systemDefault)
      } else {
        LocalDate.parse(dateStr, formatter).atStartOfDay().atZone(ZoneId.systemDefault).plusDays(1)
      }
      new Success(Date.from(ld.toInstant()))
    } catch {
      case ex: ParseException => {
        new Failure(ex)
      }
    }
  }

  private def getPlatformsAndPaymentSystems(request: Request[_]): Option[Tuple2[List[String], List[Int]]] = {
    (request.headers.get("X-Platforms"), request.headers.get("X-PaymentSystems")) match {
      case (Some(platforms), Some(paymentSystems)) => {
        Some((platforms.split(",").toList.sorted, paymentSystems.split(",").toList.map(_.toInt).sorted ))
      }
      case _ => None
    }
  }

  private def getPreviousDates(startStr: String, endStr: String): (Date, Date) = {
    def getLocalDate(dateStr: String, endDate: Boolean = false): ZonedDateTime = {
      val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
      if(!endDate){
        LocalDate.parse(dateStr, formatter).atStartOfDay().atZone(ZoneId.systemDefault)
      } else {
        LocalDate.parse(dateStr, formatter).atStartOfDay().atZone(ZoneId.systemDefault).plusDays(1)
      }
    }

    val start = getLocalDate(startStr)
    val end =  getLocalDate(endStr, true)
    val difference = Period.between(start.toLocalDate(), end.toLocalDate()).getDays()
    (Date.from(start.minusDays(difference).toInstant()), Date.from(end.minusDays(difference).toInstant()))
  }

  private def executeRequest[T <: JsValue](
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String,
    f:(String, String, Date, Date, List[String], List[Int]) => Future[T],
    platforms: List[String],
    paymentSystems: List[Int],
    requestType: Int
  ) = {
    def calculateDelta(current: JsValue, previous: JsValue): JsValue = {
      def calculateDeltaAux(currentValue: Double, previousValue: Double): Double = {
        if(currentValue > 0.0) {
          ((currentValue - previousValue) / currentValue) * 100
        } else 0.0
      }

      val totalDelta = calculateDeltaAux((current \ "value").as[Double], (previous \ "value").as[Double])
      val platformResults = platforms map {p =>
        def getPlatform(j: JsValue) = {
          (j \ "platforms").as[JsArray].value.find(e => (e \ "platform").as[String] == p).get
        }
        val platformCurrent = getPlatform(current)
        val platformPrevious = getPlatform(previous)
        val delta = calculateDeltaAux((platformCurrent \ "value").as[Double], (platformPrevious \ "value").as[Double])
        val paymentSystemsResults = paymentSystems map {system =>
          def getPaymentSystemResults(jsonArray: JsArray): Option[JsValue] = {
            jsonArray.value.toList.find(p => (p \ "system").as[Int] == system)
          }
          val currentOpt = getPaymentSystemResults((platformCurrent \ "paymentSystems").as[JsArray])
          val previousOpt = getPaymentSystemResults((platformPrevious \ "paymentSystems").as[JsArray])
          (currentOpt, previousOpt) match {
            case (Some(current), Some(previous)) => {
              Json.obj("system" -> system,
                "value" -> (current \ "value").as[Double],
                "previous" -> (previous \ "value").as[Double],
                "delta" -> calculateDeltaAux((current \ "value").as[Double], (previous \ "value").as[Double])
              )
            }
            case _ => {
              Json.obj("system" -> system, "value" -> 0.0, "previous" -> 0.0, "delta" -> 0.0)
            }
          }
        }
        Json.obj(
          "platform" -> p,
          "value" -> (platformCurrent \ "value").as[Double],
          "delta" -> delta,
          "previous" -> (platformPrevious \ "value").as[Double],
          "paymentSystems" -> paymentSystemsResults
        )
      }

      Json.obj(
        "value" -> (current \ "value").as[Double],
        "delta" -> totalDelta,
        "previous" -> (previous \ "value").as[Double],
        "platforms" -> platformResults
      )
    }

    def handleTotalRequest(startDateStr: String, endDateStr: String, s: Date, e: Date) = {
      val dates = getPreviousDates(startDateStr, endDateStr)
      val res: Future[JsValue] = for {
        currentDates <- f(companyName, applicationName, s, e, platforms, paymentSystems)
        previousDates <- f(companyName, applicationName, dates._1, dates._2, platforms, paymentSystems)
      } yield calculateDelta(currentDates, previousDates)

      res map {r =>
        Ok(r)
      } recover {
        case ex: Exception => {
          BadRequest("Error ocurred")
        }
      }
    }

    def handleDetailedRequest(start: Date, end: Date) = {
      f(companyName, applicationName, start, end, platforms, paymentSystems) map {result =>
        Ok(result)
      } recover {
        case ex: Exception => {
          ex.printStackTrace
          BadRequest("Error ocurred")
        }
      }
    }

    val start = validateDate(startDateStr)
    val end = validateDate(endDateStr, true)
    
    (start, end) match {
      case (Success(s), Success(e)) => {
        requestType match {
          case Total => handleTotalRequest(startDateStr, endDateStr, s, e)
          case Detailed => handleDetailedRequest(s, e)
        }
      }
      case _ => {
        Future {
          NotAcceptable("Invalid date format. Right format yyyy-MM-dd")
        }
      }
    }
  }

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest[JsValue](
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalARPU,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getDetailedARPU(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getARPU,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAverageRevenuePerSession,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getDetailedAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAverageRevenuePerSession,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalRevenue,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getDetailedTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getRevenue,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalLifeTimeValue,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getDetailedLifeTimeValue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getLifeTimeValue,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAveragePurchasesUser,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getDetailedAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAveragePurchasesUser,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalNumberSessionsFirstPurchase(
    companyName: String, 
    applicationName: String, 
    startDateStr: String, 
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalNumberSessionsFirstPurchase,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getNumberSessionsFirstPurchase(
    companyName: String, 
    applicationName: String, 
    startDateStr: String, 
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getNumberSessionsToFirstPurchase,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalNumberSessionsBetweenPurchases(
    companyName: String, 
    applicationName: String, 
    startDateStr: String, 
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalNumberSessionsBetweenPurchases,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getNumberSessionsBetweenPurchases(
    companyName: String, 
    applicationName: String, 
    startDateStr: String, 
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAverageTimeBetweenPurchases,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalNumberPayingCustomers,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getNumberPayingCustomers,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAveragePurchasePerSession,
        data._1,
        data._2,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatformsAndPaymentSystems(request) match {
      case Some(data) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAveragePurchasePerSession,
        data._1,
        data._2,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }
}

