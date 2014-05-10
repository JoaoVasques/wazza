package service.analytics.implementations

import models.user.PurchaseInfo
import org.bson.BSONObject
import play.api.libs.ws.WS
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.util.Failure
import service.analytics.definitions.AnalyticsService
import java.util.Date
import play.api.libs.json.JsArray
import scala.concurrent._
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class AnalyticsServiceImpl extends AnalyticsService {


  def getTopTenItems(companyName: String, applicationName: String): Future[JsArray] = {
    null
  }

  def getAverageRevenueOnTimeRange(companyName: String, applicationName: String, start: Date, end: Date): Future[Double] = {
    null
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[Double] = {

    val promise = Promise[Double]
    Play.current.configuration.getConfig("analytics") match {
      case Some(conf) => {
        val analyticsUrl = conf.underlying.root.get("url").render.filter(_ != '"')
        val url = s"${analyticsUrl}jobs?appName=test&classPath=spark.jobserver.TotalRevenue&sync=true"
        WS.url(url).post("").map {response =>
          // TODO extract ID - make query and retrive result
            println(s"response ${response.json}")
        }
      }
      case _ => promise.failure(new Exception("No analytics config"))
    }

    promise.future
  }
}

