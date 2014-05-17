package actors.analytics

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import com.google.inject._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import service.analytics.definitions.AnalyticsService
import service.security.definitions.InternalService
import play.api.Play
import models.security.CompanyData

case class TotalDailyRevenue()
case class TopItems()

class AnalyticsJobSchedulerActor extends Actor {

  private val analyticsService = Play.current.global.getControllerInstance(classOf[AnalyticsService])
  private val securityService = Play.current.global.getControllerInstance(classOf[InternalService])

  def receive = {
    case TopItems => {
      println("schedule top items..")
      // get current date and last week ()
    }

    case TotalDailyRevenue => {
      def getYesterday() = {
        val df = new SimpleDateFormat("yyyy/MM/dd")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        df.parse(df.format(cal.getTime()))
      }

      val yesterday = getYesterday
      for {
        data <- securityService.getCompanies
        app <- data.apps
      } analyticsService.calculateTotalRevenue(data.name, app, yesterday, yesterday)
    }
  }
}

