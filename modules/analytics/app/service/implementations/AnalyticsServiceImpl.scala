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

class AnalyticsServiceImpl @Inject()(
  databaseService: DatabaseService,
  purchaseService: PurchaseService
) extends AnalyticsService {

  private lazy val ProfitMargin = 0.7

  private def fillEmptyResult(start: Date, end: Date): JsArray = {
    val dates = new ListBuffer[String]()
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays()+1

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
    val days = Days.daysBetween(s, e).getDays()+1

    val futureResult = Future.sequence(List.range(0, days) map {dayIndex =>
      val currentDay = s.withFieldAdded(DurationFieldType.days(), dayIndex)
      val previousDay = currentDay.minusDays(1)
      f(companyName, applicationName, previousDay.toDate, currentDay.toDate) map {res: JsValue =>
        Json.obj(
          "day" -> currentDay.toString("dd MMM"),
          "val" -> (res \ "value").as[Float]
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
    val payingUsersFuture = databaseService.getDocumentsWithinTimeRange(collection, fields, s, e)

    payingUsersFuture map {payingUsers =>
      if(payingUsers.value.isEmpty) {
        0
      } else {
        var users = List[String]()
        for(el <- payingUsers.value) {
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

    val futureArpu = databaseService.getDocumentsWithinTimeRange(
      arpuCollection,
      fields,
      start,
      end
    )

    futureArpu map {arpu =>
      if(arpu.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        new JsArray(arpu.value map {el =>
          val day = new LocalDate((el \ "lowerDate").as[Float])
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
    val futureArpu = databaseService.getDocumentsWithinTimeRange(
      arpuCollection,
      fields,
      start,
      end
    )

    futureArpu map {arpu =>
      val res = arpu.value.foldLeft(0.0)((acc, el) => {
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
    val futureAvgRevenueSession = databaseService.getDocumentsWithinTimeRange(
      Metrics.avgRevenueSessionCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgRevenueSession map {avgRevenueSession =>
      if(avgRevenueSession.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        new JsArray(avgRevenueSession.value.map {el =>
          val day = new LocalDate((el \ "lowerDate").as[Float])
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
    val futureAvgRevenueSession = databaseService.getDocumentsWithinTimeRange(
      Metrics.avgRevenueSessionCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgRevenueSession map {avgRevenueSession =>
      val res = avgRevenueSession.value.foldLeft(0.0)((acc, el) => {
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
    val futureRevenue = databaseService.getDocumentsWithinTimeRange(
      Metrics.totalRevenueCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureRevenue map {revenue =>
      val totalRevenue = revenue.value.foldLeft(0.0)((sum, obj) => {
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
     databaseService.getDocumentsWithinTimeRange(collection, fields, start, end) map {revenue =>
       if(revenue.value.size == 0) {
         fillEmptyResult(start, end)
       } else {
         new JsArray(revenue.value map {(el: JsValue) => {
           val day = new LocalDate((el \ "lowerDate").as[Float])
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
    val futureAvgPurchasesUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.avgPurchasesUserCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgPurchasesUser map {avgPurchasesUser =>
      val res = avgPurchasesUser.value.foldLeft(0.0)((acc, el) => {
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
    val futureAvgSessionsPerUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.avgSessionsPerUserCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgSessionsPerUser map {avgSessionsPerUser =>
      val res = avgSessionsPerUser.value.foldLeft(0.0)((acc, el) => {
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
    val futureLTV = databaseService.getDocumentsWithinTimeRange(
      Metrics.lifeTimeValueCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureLTV map {ltv =>
      val res = ltv.value.foldLeft(0.0)((acc, el) => {
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
    val futureAvgTimeFirstPurchase = databaseService.getDocumentsWithinTimeRange(
      Metrics.averageTimeFirstPurchaseCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgTimeFirstPurchase map {avgPurchasesUser =>
      val res = avgPurchasesUser.value.foldLeft(0.0)((acc, el) => {
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
    val futureAvgTimeBetPurchases= databaseService.getDocumentsWithinTimeRange(
      Metrics.averageTimeBetweenPurchasesCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgTimeBetPurchases map {time =>
      val res = time.value.foldLeft(0.0)((acc, el) => {
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
    val futureAvgPurchasesPerSession= databaseService.getDocumentsWithinTimeRange(
      Metrics.averagePurchasePerSessionCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureAvgPurchasesPerSession map {purchasesPerSession =>
      val res = purchasesPerSession.value.foldLeft(0.0)((acc, el) => {
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
