package controllers.analytics

import com.google.inject._
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.analytics.definitions.AnalyticsService

class AnalyticsController @Inject()(
  analyticsService: AnalyticsService
) extends Controller {

  private def validateDate(dateStr: String): Try[Date] = {
    val df = new SimpleDateFormat("yyy-MM-dd")
    try {
      val date = df.parse(dateStr)
      new Success(date)
    } catch {
      case ex: ParseException => {
        new Failure(ex)
      }
    }
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action.async {implicit request =>
    analyticsService.getTotalRevenue(companyName, applicationName, new Date, new Date) map {res =>
      Ok(res)
    }
  }

  def test = TODO
}

