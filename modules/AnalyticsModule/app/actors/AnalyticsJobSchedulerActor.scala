package actors.analytics

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import com.google.inject._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import service.analytics.definitions.AnalyticsService
import play.api.Play

case class TotalDailyRevenue()
case class TopItems()

class AnalyticsJobSchedulerActor extends Actor {

  private val analyticsService = Play.current.global.getControllerInstance(classOf[AnalyticsService])

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
      //analyticsService.calculateTotalRevenue("cName", "appName", yesterday, yesterday)
      println(s"calculates total revenue of previous day - $yesterday")
    }
  }
}

