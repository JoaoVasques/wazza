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

  private def executeRequest[T <: JsValue](
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String,
    f:(String, String, Date, Date) => Future[T],
    requestType: Int
  ) = {
    def calculateDelta(current: JsValue, previous: JsValue): JsValue = {
      val currentValue = (current \ "value").as[Double]
      val previousValue = (current \ "value").as[Double]

      val delta = if(currentValue > 0) {
        (currentValue - previousValue) / currentValue
      } else 0

      Json.obj(
        "value" -> currentValue,
        "delta" -> delta
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
      Total)
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
      Detailed)
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
      Total)
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
      Detailed)
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
      Total)
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
      Detailed)
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
      Total)
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
      Detailed)
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
      Total)
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
      Detailed)
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
      Total)
  }
}
