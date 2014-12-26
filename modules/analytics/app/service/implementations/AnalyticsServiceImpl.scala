package service.analytics.implementations

import java.text.SimpleDateFormat
import models.user.MobileSession
import models.user.PurchaseInfo
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
import service.persistence.definitions.DatabaseService
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.DurationFieldType
import org.joda.time.DateTime
import service.user.definitions.PurchaseService
import persistence.utils.{DateUtils}
import persistence._
import persistence.messages._
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.{Timeout}
import scala.collection.mutable.Stack

class AnalyticsServiceImpl @Inject()(
  databaseService: DatabaseService,
  purchaseService: PurchaseService
) extends AnalyticsService {

  private lazy val ProfitMargin = 0.7
  private val databaseProxy = PersistenceProxy.getInstance()
  private implicit val timeout = Timeout(8 seconds)

  private def fillEmptyResult(start: Date, end: Date): JsArray = {
    val dates = new ListBuffer[String]()
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    new JsArray(List.range(0, days) map {i =>{
      Json.obj(
        "day" -> s.withFieldAdded(DurationFieldType.days(), i).toString("dd MMM"),
        "value" -> 0
      )
    }})
  }

  private def calculateDetailedKPIAux(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    f:(String, String, Date, Date) => Future[JsValue]
  ): Future[JsArray] = {
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    val futureResult = Future.sequence(List.range(0, days) map {dayIndex =>
      val currentDay = s.withFieldAdded(DurationFieldType.days(), dayIndex)
      val previousDay = currentDay.minusDays(1)
      f(companyName, applicationName, previousDay.toDate, currentDay.toDate) map {res: JsValue =>
        Json.obj(
          "day" -> currentDay.toString("dd MMM"),
          "value" -> (res \ "value").as[Float]
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
    e: Date
  ): Future[Float] = {
    val collection = Metrics.payingUsersCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, s, e, true)
    val payingUsersFuture = (databaseProxy ? request).mapTo[PRJsArrayResponse]

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
    val arpuCollection = Metrics.arpuCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")
    val request = new GetDocumentsWithinTimeRange(new Stack, arpuCollection, fields, start, end, true)
    val futureArpu = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureArpu map {arpu =>
      if(arpu.res.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        new JsArray(arpu.res.value map {el =>
          val day = new LocalDate((el \ "lowerDate").as[Double].longValue)
          Json.obj(
            "day" -> day.toString("dd MM"),
            "value" -> (el \ "arpu").as[Double]
          )
        })
      }
    }
  }

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val arpuCollection = Metrics.arpuCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")
    val request = new GetDocumentsWithinTimeRange(new Stack, arpuCollection, fields, start, end, true)
    val futureArpu = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureArpu map {arpu =>
      val res = arpu.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "arpu").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgRevenueSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgRevenueSession = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgRevenueSession map {avgRevenueSession =>
      if(avgRevenueSession.res.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        new JsArray(avgRevenueSession.res.value.map {el =>
          val day = new LocalDate((el \ "lowerDate").as[Double].longValue)
          Json.obj(
            "day" -> day.toString("dd MM"),
            "value" -> (el \ "avgRevenueSession").as[Double]
          )
        })
      }
    }
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgRevenueSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgRevenueSession = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgRevenueSession map {avgRevenueSession =>
      val res = avgRevenueSession.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "avgRevenueSession").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.totalRevenueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRevenue = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureRevenue map {revenue =>
      val totalRevenue = revenue.res.value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      })
      Json.obj("value" -> totalRevenue)
    }
  }

  def getRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    val collection = Metrics.totalRevenueCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
      (databaseProxy ? request).mapTo[PRJsArrayResponse] map {revenue =>
       if(revenue.res.value.size == 0) {
         fillEmptyResult(start, end)
       } else {
         new JsArray(revenue.res.value map {(el: JsValue) => {
           println(el)
           val day = new LocalDate((el \ "lowerDate").as[Double].longValue)
           Json.obj(
             "day" -> day.toString("dd MM"),
             "value" -> (el \ "totalRevenue").as[Int]
           )
         }})
       }
     }
  }

  def getTotalAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgPurchasesUserCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgPurchasesUser = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgPurchasesUser map {avgPurchasesUser =>
      val res = avgPurchasesUser.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "avgPurchasesUser").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalAveragePurchasesUser)
  }

  def getTotalAverageNumberSessionsPerUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
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
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.lifeTimeValueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureLTV = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureLTV map {ltv =>
      val res = ltv.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "lifeTimeValue").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalLifeTimeValue)
  }

  //TODO
  def getTotalAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.averageTimeFirstPurchaseCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgTimeFirstPurchase = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgTimeFirstPurchase map {avgPurchasesUser =>
      val res = avgPurchasesUser.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "avgTimeFirstPurchase").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalAverageTimeFirstPurchase)
  }

  def getTotalAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.averageTimeBetweenPurchasesCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgTimeBetPurchases = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgTimeBetPurchases map {time =>
      val res = time.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "avgTimeBetweenPurchases").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalAverageTimeBetweenPurchases)
  }

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    calculateNumberPayingCustomers(
      companyName,
      applicationName,
      ("lowerDate", "upperDate"),
      start,
      end
    ) map {payingCustomers => Json.obj("value" -> payingCustomers)}
  }

  def getNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalNumberPayingCustomers)
  }

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.averagePurchasePerSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgPurchasesPerSession = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureAvgPurchasesPerSession map {purchasesPerSession =>
      val res = purchasesPerSession.res.value.foldLeft(0.0)((acc, el) => {
        acc + (el \ "avgPurchasesSession").as[Double]
      })

      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      Json.obj("value" -> (if(days > 0) res / days else res))
    }
  }

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalAveragePurchasePerSession)
  }
}
