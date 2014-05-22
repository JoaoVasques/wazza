package actors.analytics

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import com.google.inject._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import play.api.Logger
import play.api.libs.json.JsResultException
import service.analytics.definitions.AnalyticsService
import service.security.definitions.InternalService
import play.api.Play
import models.security.CompanyData
import scala.concurrent._
import ExecutionContext.Implicits.global

case class TotalDailyRevenue()
case class TopItems()

private trait Time
private case class Today() extends Time
private case class Yesterday() extends Time

class AnalyticsJobSchedulerActor extends Actor {

  private val analyticsService = Play.current.global.getControllerInstance(classOf[AnalyticsService])
  private val securityService = Play.current.global.getControllerInstance(classOf[InternalService])

  def receive = {
    case TopItems => {
      println("schedule top items..")
      // get current date and last week ()
    }

    case TotalDailyRevenue => {
      def getDate(time: Time) = {
        val df = new SimpleDateFormat("yyyy/MM/dd")
        val cal = Calendar.getInstance()

        time match {
          case t: Yesterday => cal.add(Calendar.DATE, -1)
          case _ => {}
        }

        df.parse(df.format(cal.getTime()))
      }

      val yesterday = getDate(new Yesterday)
      val today = getDate(new Today)
      for {
        data <- securityService.getCompanies
        app <- data.apps
      } analyticsService.calculateTotalRevenue(data.name, app, yesterday, today) map { result =>
        try {
          val jobResult = (result \ "status").as[String]
          jobResult match {
            case "STARTED" => {
              Logger.info(s"Calculate Revenue of app $app from ${data.name} started")
            }
            case _ => {
              Logger.error(s"Unkown job server reply status - $jobResult")
            }
          }
        } catch {
          case e: JsResultException => {
            Logger.error("Parse job result exception", e)
          }
        }
      }
    }
  }
}

