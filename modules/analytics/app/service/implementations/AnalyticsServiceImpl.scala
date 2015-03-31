package service.analytics.implementations

import java.text.SimpleDateFormat
import models.user.MobileSession
import models.payments.PurchaseInfo
import org.bson.BSONObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.StringOps
import scala.collection.mutable.Map
import scala.util.Failure
import service.analytics.definitions.AnalyticsService
import java.util.Date
import play.api.libs.json.JsArray
import scala.concurrent._
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.analytics.Metrics
import com.google.inject._
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.DurationFieldType
import org.joda.time.DateTime
import persistence.utils.{DateUtils}
import persistence._
import persistence.messages._
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.{Timeout}
import scala.collection.mutable.Stack

class AnalyticsServiceImpl extends AnalyticsService {

  private lazy val ProfitMargin = 0.7
  private val databaseProxy = PersistenceProxy.getInstance()
  private implicit val timeout = Timeout(8 seconds)

  private def fillEmptyResult(start: Date, end: Date, platforms: List[String]): JsArray = {
    val dates = new ListBuffer[String]()
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    new JsArray(List.range(0, days) map {i =>{
      Json.obj(
        "day" -> s.withFieldAdded(DurationFieldType.days(), i).toDate.getTime,
        "value" -> 0.0,
        "platforms" -> (platforms map {p => Json.obj("value" -> 0.0, "platform" -> p)})
      )
    }})
  }

  protected def parseDate(json: JsValue, key: String): Date = {
    val dateStr = (json \ key \ "$date").as[String]
    val ops = new StringOps(dateStr)
    new SimpleDateFormat("yyyy-MM-dd").parse(ops.take(ops.indexOf('T')))
  }

  private def calculateDetailedKPIAux(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    f:(String, String, Date, Date, List[String]) => Future[JsValue]
  ): Future[JsArray] = {
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    val futureResult = Future.sequence(List.range(0, days) map {dayIndex =>
      val currentDay = s.withFieldAdded(DurationFieldType.days(), dayIndex)
      val previousDay = currentDay.minusDays(1)
      f(companyName, applicationName, previousDay.toDate, currentDay.toDate, platforms) map {res: JsValue =>
        Json.obj(
          "day" -> currentDay.toDate.getTime,
          "value" -> (res \ "value").as[Double],
          "platforms" -> (platforms map {p => {
            val platformInfo = getPlatform(res, p)
            Json.obj(
              "platform" -> ((platformInfo \ "platform").as[String]),
              "value" -> ((platformInfo \ "value").as[Double])
            )
          }})
        )
      }
    })

    futureResult map {jsonSeq =>
      new JsArray(jsonSeq)
    }
  }

  private def calculateNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    fields: Tuple2[String, String],
    s: Date,
    e: Date,
    platform: Option[String] = None
  ): Future[Float] = {
    val collection = Metrics.payingUsersCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, s, e, true)
    val payingUsersFuture = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    platform match {
      case Some(platform) => {
        null
      }
      case None => {
        payingUsersFuture map {payingUsers =>
          if(payingUsers.res.value.isEmpty) {
            0
          } else {
            var users = List[String]()
            for(el <- payingUsers.res.value) {
              val userId = (el \ "userId").as[String]
              users = (userId :: users).distinct
            }
            users.size
          }
        }
      }
    }
  }

  // Gets list of platforms from JSON retrived from database
  private def getPlatform(json: JsValue, platform: String): JsValue = {
    (json \ "platforms").as[JsArray].value.find(e => (e \ "platform").as[String] == platform).get
  }

  private def getDetailedResult(data: Seq[JsValue], platforms: List[String]): JsValue = {
    val emptyResult = Json.obj(
      "value" -> 0.0,
      "platforms" -> (platforms map {p => Json.obj("platform" -> p, "value" -> 0.0)})
    )
    data.foldLeft(emptyResult)((res, current) => {
      val updateResult = (res \ "value").as[Double] + (current \ "result").as[Double]
      val platformResults = platforms map {platform =>
        val resPlatform = getPlatform(res, platform)
        val currentPlatform = getPlatform(current, platform)
        val updatedValue = (resPlatform \ "value").as[Double] + (currentPlatform \ "res").as[Double]
        Json.obj("platform" -> platform, "value" -> updatedValue)
      }
      Json.obj("value" -> updateResult, "platforms" -> platformResults)
    })
  }

  private def getAverageOfTotalResults(
    data: Seq[JsValue],
    platforms: List[String],
    start: Date,
    end: Date
  ): JsValue = {
    def averagenizer(v: Double): Double = {
      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      if(days > 0) v / days else v
    }

    val res = getDetailedResult(data, platforms)
    Json.obj(
      "value" -> averagenizer((res \ "value").as[Double]),
      "platforms" -> ((res \ "platforms").as[JsArray].value map {p =>
        Json.obj(
          "platform" -> ((p \ "platform").as[String]),
          "value" -> averagenizer((p \ "value").as[Double])
        )
      })
    )
  }

  def getARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalARPU)
  }

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val arpuCollection = Metrics.arpuCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")
    val request = new GetDocumentsWithinTimeRange(new Stack, arpuCollection, fields, start, end, true)
    val futureArpu = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureArpu map {r =>
      getAverageOfTotalResults(r.res.value, platforms, start, end)
    }
  }

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalAverageRevenuePerSession)
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgRevenueSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgRevenueSession = (databaseProxy ? request).mapTo[PRJsArrayResponse]


    def averagenizer(v: Double): Double = {
      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      if(days > 0) v / days else v
    }

    futureAvgRevenueSession map {r =>
      val res = getDetailedResult(r.res.value, platforms)
      Json.obj(
        "value" -> averagenizer((res \ "value").as[Double]),
        "platforms" -> ((res \ "platforms").as[JsArray].value map {p =>
          Json.obj(
            "platform" -> ((p \ "platform").as[String]),
            "value" -> averagenizer((p \ "value").as[Double])
          )
        })
      )
    }
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.totalRevenueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRevenue = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    futureRevenue map {r => getDetailedResult(r.res.value, platforms)}
  }

  def getRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalRevenue)
  }

  def getTotalAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgPurchasesUserCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {r =>
      getAverageOfTotalResults(r.res.value, platforms, start, end)
    }
  }

  def getAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalAveragePurchasesUser)
  }

  // TODO
  def getTotalAverageNumberSessionsPerUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgSessionsPerUserCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgSessionsPerUser = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgSessionsPerUser map {avgSessionsPerUser =>
      val res = avgSessionsPerUser.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "nrSessions").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.lifeTimeValueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {ltv =>
      getAverageOfTotalResults(ltv.res.value, platforms, start, end)
    }
  }

  def getLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalLifeTimeValue)
  }

  def getTotalNumberSessionsFirstPurchase(
    companyName: String, 
    applicationName: String, 
    start: Date, 
    end: Date, 
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.sessionsFirstPurchase(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRes = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureRes map {result =>
      val emptyRes = (0.0, platforms map{(_, 0.0)})
      val res = result.res.value.foldLeft(emptyRes)((acc, current) => {
        val sessions = acc._1 + (current \ "result").as[Double]
        val pInfo = (current \ "platforms").as[JsArray].value.toList
        val platformData = acc._2 map {p =>
          pInfo.find(pp => (pp \ "platform").as[String] == p._1) match {
            case Some(info) => (p._1, p._2 + (info \ "result").as[Double])
            case None => (p._1, p._2)
          }
        }
        (sessions, platformData)
      })

      def averagenizer(v: Double): Double = {
        val days = DateUtils.getNumberDaysBetweenDates(start, end)
        if(days > 0) v / days else v
      }

      Json.obj(
        "value" -> averagenizer(res._1),
        "platforms" -> (res._2 map {p =>
          Json.obj(
            "platform" -> p._1,
            "value" -> averagenizer(p._2)
          )
        })
      )
    }
  }

  def getNumberSessionsToFirstPurchase(
    companyName: String, 
    applicationName: String, 
    start: Date, 
    end: Date, 
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalNumberSessionsFirstPurchase)
  }

  def getTotalNumberSessionsBetweenPurchases(
    companyName: String, 
    applicationName: String, 
    start: Date, 
    end: Date, 
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.sessionsBetweenPurchasesCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {time =>
      /**
        FORMAT: (sessions, users List(platform, result))
      **/
      val emptyResult = (0.0, 0.0, platforms map{ (_, 0.0)})
      val result = time.res.value.foldLeft(emptyResult){(acc, current) => {
        val currentElement = current
        val sessions = (current \ "totalSessions").as[Double] + acc._1
        val users = (current \ "numberUsers").as[Double] + acc._2
        val pData = (current \ "platforms").as[JsArray].value.toList
        val platformData = acc._3 map {platformElement =>
          val platform = platformElement._1
          pData.find(p => (p \ "platform").as[String] == platform) match {
            case Some(info) => {
              val res = (info \ "res").as[Double] + platformElement._2
              (platform, res)
            }
            case None => {
              (platform, platformElement._2)
            }
          }
        }
        (sessions, users, platformData)
      }}
      def averagenizer(v: Double): Double = {
        val days = DateUtils.getNumberDaysBetweenDates(start, end)
        if(days > 0) v / days else v
      }
      val finalTime = averagenizer(if(result._2 > 0) result._1 / result._2 else 0.0)
      Json.obj(
        "value" -> finalTime,
        "platforms" -> (result._3 map {p =>
          Json.obj(
            "platform" -> p._1,
            "value" -> averagenizer(p._2)
          )
        })
      )
    }
  }

  def getAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalNumberSessionsBetweenPurchases)
  }

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val collection = Metrics.payingUsersCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, ("lowerDate", "upperDate"), start, end, true)
    val empty = Json.obj("value" -> 0, "platforms" -> (platforms map {p => Json.obj("platform" -> p, "value" -> 0)}))
    (databaseProxy ? request).mapTo[PRJsArrayResponse] map {r =>
      if(r.res.value.isEmpty) {
        empty
      } else {
        r.res.value.foldLeft(empty){(res, current) => {
          val totalUpdated = (res \ "value").as[Int] + 1
          val updatedPlatforms = platforms map {platform =>
            val pInfo = (res \ "platforms").as[JsArray].value.find(p => (p \ "platform").as[String] == platform).get
            Json.obj("platform" -> platform, "value" -> ((pInfo \ "value").as[Int] + 1))
          }
          Json.obj("value" -> totalUpdated, "platforms" -> updatedPlatforms)
        }}
      }
    }
  }

  def getNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalNumberPayingCustomers)
  }

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.averagePurchasePerSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {p =>
      getAverageOfTotalResults(p.res.value, platforms, start, end)
    }
  }

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, getTotalAveragePurchasePerSession)
  }
}

