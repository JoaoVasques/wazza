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

class AnalyticsController @Inject()(
  analyticsService: AnalyticsService
) extends Controller {

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
    f:(String, String, Date, Date) => Future[T]
  ) = {

    val start = validateDate(startDateStr)
    val end = validateDate(endDateStr)

    (start, end) match {
      case (Success(s), Success(e)) => {
        val dates = getPreviousDates(startDateStr, endDateStr)
        f(companyName, applicationName, s, e) map { res =>
          Ok(res)
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
      analyticsService.getTotalARPU)
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
      analyticsService.getARPU)
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
      analyticsService.getTotalRevenue)
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
      analyticsService.getRevenue)
  }
}

