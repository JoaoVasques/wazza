package service.analytics.implementations

import java.text.SimpleDateFormat
import models.user.PurchaseInfo
import org.bson.BSONObject
import play.api.libs.json.JsValue
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
import utils.analytics.Metrics

class AnalyticsServiceImpl extends AnalyticsService {


  def calculateTopTenItems(companyName: String, applicationName: String, start: Date, end: Date) = {

  }

  def getTopTenItems(companyName: String, applicationName: String, start: Date, end: Date): Future[JsArray] = {
    null
  }

  def calculateTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    /**
      Format: inputCollectionName outputCollectionName startEnd endDate
      **/
    def generateContent() = {
      val df = new SimpleDateFormat("yyyy/MM/dd")
      val inputCollection = PurchaseInfo.getCollection(companyName, applicationName)
      val outputCollection = Metrics.totalRevenueCollection(companyName, applicationName)
      s"input=$inputCollection $outputCollection ${df.format(start)} ${df.format(end)}"
    }

    val promise = Promise[JsValue]
    Play.current.configuration.getConfig("analytics") match {
      case Some(conf) => {
        val df = new SimpleDateFormat("yyyy/MM/dd")
        val analyticsUrl = conf.underlying.root.get("url").render.filter(_ != '"')
        val url = s"${analyticsUrl}jobs?appName=test&classPath=spark.jobserver.TotalRevenue"
        WS.url(url).post(generateContent).map {response =>
          promise.success(response.json)
        }
      }
      case _ => promise.failure(new Exception("No analytics config"))
    }

    promise.future
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[Double] = {

    /**
      Format: inputCollectionName outputCollectionName startEnd endDate
    **/
    def generateContent() = {
      val df = new SimpleDateFormat("yyyy/MM/dd")
      val inputCollection = PurchaseInfo.getCollection(companyName, applicationName)
      val outputCollection = Metrics.totalRevenueCollection(companyName, applicationName)
      s"input=$inputCollection $outputCollection ${df.format(start)} ${df.format(end)}"
    }

    val promise = Promise[Double]
    Play.current.configuration.getConfig("analytics") match {
      case Some(conf) => {
        val df = new SimpleDateFormat("yyyy/MM/dd")
        val analyticsUrl = conf.underlying.root.get("url").render.filter(_ != '"')
        val url = s"${analyticsUrl}jobs?appName=test&classPath=spark.jobserver.TotalRevenue&sync=true"
        WS.url(url).post(generateContent).map {response =>
          // TODO extract ID - make query and retrive result
            println(s"response ${response.json}")
        }
      }
      case _ => promise.failure(new Exception("No analytics config"))
    }

    promise.future
  }
}

