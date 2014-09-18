package service.analytics.implementations

import java.text.SimpleDateFormat
import models.user.MobileSession
import models.user.PurchaseInfo
import org.bson.BSONObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.ws.WS
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.StringOps
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
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.DurationFieldType

class AnalyticsServiceImpl @Inject()(
  databaseService: DatabaseService
) extends AnalyticsService {

  private def getUnixDate(dateStr: String): Long = {
    val ops = new StringOps(dateStr)
    (new SimpleDateFormat("yyyy-MM-dd").parse(ops.take(ops.indexOf('T'))).getTime()) / 1000
  }

  def getTopTenItems(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    //TODO
    null
  }

  def getARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    val promise = Promise[JsArray]

    Future {
      val revenueCollection = Metrics.totalRevenueCollection(companyName, applicationName)
      val activeUsersCollection = Metrics.activeUsersCollection(companyName, applicationName)
      val fields = ("lowerDate", "upperDate")

      val revenue = databaseService.getDocumentsWithinTimeRange(
        revenueCollection,
        fields,
        start,
        end
      ).value

      val active = databaseService.getDocumentsWithinTimeRange(
        activeUsersCollection,
        fields,
        start,
        end
      ).value

      val result = if(active.isEmpty) {
        val dates = new ListBuffer[String]()
        val s = new LocalDate(start)
        val e = new LocalDate(end)
        val days = Days.daysBetween(s, e).getDays()+1
        
        new JsArray(List.range(0, days) map {i =>{
          Json.obj(
            "day" -> s.withFieldAdded(DurationFieldType.days(), i).toString("dd MMM"),
            "val" -> 0
          )
        }})
      } else {
        new JsArray((revenue zip active) map {
          case (r, a) => {
            Json.obj(
              "day" -> getUnixDate((r \ "lowerDate" \ "$date").as[String]),
              "value" -> (r \ "totalRevenue").as[Double] / (a \ "activeUsers").as[Int]
            )
          }
        })
      }

      promise.success(result)
    }
    promise.future
  }

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]

    Future {
      val fields = ("lowerDate", "upperDate")
      val revenue = databaseService.getDocumentsWithinTimeRange(
        Metrics.totalRevenueCollection(companyName, applicationName),
        fields,
        start,
        end
      ).value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      })

      val activeUsers = databaseService.getDocumentsWithinTimeRange(
        Metrics.activeUsersCollection(companyName, applicationName),
        fields,
        start,
        end
      ).value.foldLeft(0)((sum, obj) => {
        sum + (obj \ "activeUsers").as[Int]
      })

      promise.success(Json.obj("value" -> (if(activeUsers > 0) (revenue / activeUsers) else 0)))
    }

    promise.future
  }

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    println(s"getAverageRevenuePerSession $companyName | $applicationName")
    //TODO
    promise.future
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    println(s"getTotalAverageRevenuePerSession $companyName | $applicationName")

    val sessions = databaseService.getDocumentsByTimeRange(
      Metrics.mobileSessionsCollection(companyName, applicationName),
      "startTime",
      start,
      end
    ).value.size

    val fields = ("lowerDate", "upperDate")
    val revenue = databaseService.getDocumentsWithinTimeRange(
      Metrics.totalRevenueCollection(companyName, applicationName),
      fields,
      start,
      end
    ).value.foldLeft(0.0)((sum, obj) => {
      sum + (obj \ "totalRevenue").as[Double]
    })

    promise.success(Json.obj("value" -> (if(sessions > 0) (revenue / sessions) else 0)))
    promise.future
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    Future {
      val fields = ("lowerDate", "upperDate")
      val revenue = databaseService.getDocumentsWithinTimeRange(
        Metrics.totalRevenueCollection(companyName, applicationName),
        fields,
        start,
        end
      ).value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      })
      promise.success(Json.obj("value" -> revenue))
    }

    promise.future
  }

  def getRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    val promise = Promise[JsArray]
    Future {
      val collection = Metrics.totalRevenueCollection(companyName, applicationName)
      val fields = ("lowerDate", "upperDate")
      val results = new JsArray(
        databaseService.getDocumentsWithinTimeRange(collection, fields, start, end).value map {(el: JsValue) => {
        Json.obj(
          "timestamp" -> getUnixDate((el \ "lowerDate" \ "$date").as[String]),
          "value" -> (el \ "totalRevenue").as[Int]
        )
      }})

      promise.success(results)
    }

    promise.future
  }
}

