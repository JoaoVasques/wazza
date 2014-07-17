package service.analytics.implementations

import java.text.SimpleDateFormat
import models.user.MobileSession
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
import com.google.inject._
import service.persistence.definitions.DatabaseService

class AnalyticsServiceImpl @Inject()(
  databaseService: DatabaseService
) extends AnalyticsService {

  private val TopItems = 0
  private val SessionLength = 1
  private val TotalRevenue = 2
  private val PayingUsers = 3

  private val AnalyticsUrl = Play.current.configuration.getConfig("analytics") match {
    case Some(conf) =>conf.underlying.root.get("url").render.filter(_ != '"')
    case _ => promise.failure(new Exception("No analytics config"))
  }

  /**
    Format: inputCollectionName outputCollectionName startEnd endDate
  **/
  private def generateContent(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    rType: Int
  ): String = {
    val df = new SimpleDateFormat("yyyy/MM/dd")
    var inputCollection = "" //PurchaseInfo.getCollection(companyName, applicationName)
    var outputCollection = "" //Metrics.totalRevenueCollection(companyName, applicationName)
    rType match {
      case TopItems => {
        
      }
      case SessionLength => {
        inputCollection = MobileSession.getCollection(companyName, applicationName)
        outputCollection = Metrics.avgSessionLengthCollection(companyName, applicationName)
      }
      case TotalRevenue => {
        inputCollection = PurchaseInfo.getCollection(companyName, applicationName)
        outputCollection = Metrics.totalRevenueCollection(companyName, applicationName)
      }
      case PayingUsers => {
        inputCollection = PurchaseInfo.getCollection(companyName, applicationName)
        outputCollection = Metrics.numberPayingUsers(companyName, applicationName)
      }
    }
    s"input=$inputCollection $outputCollection ${df.format(start)} ${df.format(end)}"
  }

  def calculateTopItems(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    limit: Int
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    val url = s"${AnalyticsUrl}jobs?appName=test&classPath=spark.jobserver.TopItems"
    WS.url(url).post(generateContent(companyName, applicationName, start, end, TopItems)).map {response =>
      promise.success(response.json)
    }

    promise.future
  }

  def calculateAverageSessionLength(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    val url = s"${AnalyticsUrl}jobs?appName=test&classPath=spark.jobserver.SessionLength"
    WS.url(url).post(generateContent(
      companyName,applicationName,
      start, end,
      SessionLength
    )) map { response =>
      promise.success(response.json)
    }
    promise.future
  }

  def calculateTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]

    val url = s"${AnalyticsUrl}jobs?appName=test&classPath=spark.jobserver.TotalRevenue"
    WS.url(url).post(generateContent(companyName, applicationName, start, end, TotalRevenue)).map {response =>
      promise.success(response.json)
    }

    promise.future
  }

  def calculateNumberPayingUsers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]

    val url = s"${AnalyticsUrl}jobs?appName=test&classPath=spark.jobserver.NumberPayingUsers"
    WS.url(url).post(generateContent(companyName, applicationName, start, end, PayingUsers)).map {response =>
      promise.success(response.json)
    }

    promise.future
  }

  def getTopTenItems(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    null
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {

    val promise = Promise[JsArray]
    Future {
      val collection = Metrics.totalRevenueCollection(companyName, applicationName)
      val fields = ("lowerDate", "upperDate")
      promise.success(databaseService.getDocumentsWithinTimeRange(collection, fields, start, end))
    }

    promise.future
  }
}

