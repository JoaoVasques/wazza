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

  private def getPreviousDates(startStr: String, endStr: String): (Date, Date) = {
    val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")
    val start = formatter.parseDateTime(startStr)
    val end = formatter.parseDateTime(endStr)
    val difference = Days.daysBetween(start, end).getDays()
    (start.minusDays(difference).toDate, end.minusDays(difference).toDate)
  }

  private val deltas = List(1.2, 11.2, 5.6, 4.34, 0, 7.21, 4.5, -2.1, -3.1, 0)

  private def executeRequest[T <: JsValue](
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String,
    f:(String, String, Date, Date) => Future[T],
    requestType: Int,
    index: Int
  ) = {
    def calculateDelta(current: JsValue, previous: JsValue): JsValue = {
      val currentValue = (current \ "value").as[Double]
      val previousValue = (current \ "value").as[Double]

      val delta = if(currentValue > 0) {
        (currentValue - previousValue) / currentValue
      } else 0

      Json.obj(
        "value" -> currentValue,
        "delta" -> deltas(index)//delta
      )
    }

    def handleTotalRequest(startDateStr: String, endDateStr: String, s: Date, e: Date) = {
      val dates = getPreviousDates(startDateStr, endDateStr)
      val res: Future[JsValue] = for {
        currentDates <- f(companyName, applicationName, s, e)
        previousDates <- f(companyName, applicationName, dates._1, dates._2)
      } yield calculateDelta(currentDates, previousDates)

      res map {r =>
        Ok(r)
      } recover {
        case ex: Exception => {
          println(ex)
          BadRequest("Error ocurred")
        }
      }
    }

    def handleDetailedRequest(start: Date, end: Date) = {
      f(companyName, applicationName, start, end) map {result =>
        Ok(result)
      } recover {
        case ex: Exception => {
          println(ex)
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

    executeRequest[JsValue](
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalARPU,
      Total,
      1)
  }

  def getDetailedARPU(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getARPU,
      Detailed,
      1)
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalAverageRevenuePerSession,
      Total,
      2)
  }

  def getDetailedAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getAverageRevenuePerSession,
      Detailed,
      2)
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalRevenue,
      Total,
      3)
  }

  def getDetailedTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getRevenue,
      Detailed,
      3)
  }

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalLifeTimeValue,
      Total,
      4)
  }

  def getDetailedLifeTimeValue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getLifeTimeValue,
      Detailed,
      4)
  }

  def getTotalChurnRate(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalChurnRate,
      Total,
      5)
  }
  
  def getDetailedChurnRate(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getChurnRate,
      Detailed,
      5)
  }

  def getTotalAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalAverageTimeFirstPurchase,
      Total,
      6)
  }

  def getAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getAverageTimeFirstPurchase,
      Detailed,
      6)
  }

  def getTotalAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalAverageTimeBetweenPurchases,
      Total,
      7)
  }

  def getAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getAverageTimeBetweenPurchases,
      Detailed,
      7)
  }

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalNumberPayingCustomers,
      Total,
      8)
  }

  def getNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getNumberPayingCustomers,
      Detailed,
      8)
  }

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getTotalAveragePurchasePerSession,
      Total,
      9)
  }

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    executeRequest(
      companyName,
      applicationName,
      startDateStr,
      endDateStr,
      analyticsService.getAveragePurchasePerSession,
      Detailed,
      9)
  }
}

