package controllers.analytics

import com.google.inject._
import java.text.ParseException
import java.text.SimpleDateFormat
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
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import org.joda.time.Days
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray

class AnalyticsController @Inject()(
  analyticsService: AnalyticsService
) extends Controller {

  private lazy val Total = 0
  private lazy val Detailed = 1

  private def validateDate(dateStr: String): Try[Date] = {
    val df = new SimpleDateFormat("dd-MM-yyyy")
    try {
      val date = df.parse(dateStr)
      new Success(date)
    } catch {
      case ex: ParseException => {
        new Failure(ex)
      }
    }
  }

  private def getPlatforms(request: Request[_]): Option[List[String]] = {
    request.headers.get("X-Platforms") match {
      case Some(platformsStr) => Some(platformsStr.split(",").toList.sorted)
      case _ => None
    }
  }

  private def getPreviousDates(startStr: String, endStr: String): (Date, Date) = {
    val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")
    val start = formatter.parseDateTime(startStr)
    val end = formatter.parseDateTime(endStr)
    val difference = Days.daysBetween(start, end).getDays()
    (start.minusDays(difference).toDate, end.minusDays(difference).toDate)
  }

  private def executeRequest[T <: JsValue](
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String,
    f:(String, String, Date, Date, List[String]) => Future[T],
    platforms: List[String],
    requestType: Int
  ) = {
    def calculateDelta(current: JsValue, previous: JsValue): JsValue = {
      def calculateDeltaAux(currentValue: Double, previousValue: Double): Double = {
        if(currentValue > 0.0) {
          (currentValue - previousValue) / currentValue
        } else 0.0
      }

      val totalDelta = calculateDeltaAux((current \ "value").as[Double], (current \ "value").as[Double])
      val platformResults = platforms map {p =>
        def getPlatform(j: JsValue) = {
          (j \ "platforms").as[JsArray].value.find(e => (e \ "platform").as[String] == p).get
        }
        val platformCurrent = getPlatform(current)
        val platformPrevious = getPlatform(previous)
        val delta = calculateDeltaAux((platformCurrent \ "value").as[Double], (platformPrevious \ "value").as[Double])
        Json.obj("platform" -> p, "value" -> (platformCurrent \ "value").as[Double], "delta" -> delta)
      }

      Json.obj(
        "value" -> (current \ "value").as[Double],
        "delta" -> totalDelta,
        "platforms" -> platformResults
      )
    }

    def handleTotalRequest(startDateStr: String, endDateStr: String, s: Date, e: Date) = {
      val dates = getPreviousDates(startDateStr, endDateStr)
      val res: Future[JsValue] = for {
        currentDates <- f(companyName, applicationName, s, e, platforms)
        previousDates <- f(companyName, applicationName, dates._1, dates._2, platforms)
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
      f(companyName, applicationName, start, end, platforms) map {result =>
        Ok(result)
      } recover {
        case ex: Exception => {
          ex.printStackTrace
          BadRequest("Error ocurred")
        }
      }
    }

    val start = validateDate(startDateStr)
    val end = validateDate(endDateStr)

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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest[JsValue](
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalARPU,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getARPU,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAverageRevenuePerSession,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAverageRevenuePerSession,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalRevenue,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getRevenue,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalLifeTimeValue,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getLifeTimeValue,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAveragePurchasesUser,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAveragePurchasesUser,
        platforms,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAverageTimeFirstPurchase,
        platforms,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAverageTimeFirstPurchase,
        platforms,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getTotalAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAverageTimeBetweenPurchases,
        platforms,
        Total)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }

  def getAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAverageTimeBetweenPurchases,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalNumberPayingCustomers,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getNumberPayingCustomers,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getTotalAveragePurchasePerSession,
        platforms,
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
    getPlatforms(request) match {
      case Some(platforms) => executeRequest(
        companyName,
        applicationName,
        startDateStr,
        endDateStr,
        analyticsService.getAveragePurchasePerSession,
        platforms,
        Detailed)
      case _ => Future.successful(BadRequest("Please select a platform"))
    }
  }
}

