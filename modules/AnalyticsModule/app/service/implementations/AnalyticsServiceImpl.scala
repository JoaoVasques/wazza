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

class AnalyticsServiceImpl @Inject()(
  databaseService: DatabaseService,
  purchaseService: PurchaseService
) extends AnalyticsService {

  lazy val ProfitMargin = 0.7 // Because Google and Apple take a 30% on every purchase

/**  private def getUnixDate(dateStr: String): Long = {
    val ops = new StringOps(dateStr)
    (new SimpleDateFormat("yyyy-MM-dd").parse(ops.take(ops.indexOf('T'))).getTime()) / 1000
  }**/

  private def getDateFromString(dateStr: String): Date = {
    val ops = new StringOps(dateStr)
    new SimpleDateFormat("yyyy-MM-dd").parse(ops.take(ops.indexOf('T')))
  }

  private def getNumberDaysBetweenDates(d1: Date, d2: Date): Int = {
    Days.daysBetween(new LocalDate(d1), new LocalDate(d2)).getDays()
  }

  private def getNumberSecondsBetweenDates(d1: Date, d2: Date): Float = {
    (new LocalDate(d2).toDateTimeAtCurrentTime.getMillis - new LocalDate(d1).toDateTimeAtCurrentTime().getMillis) / 1000
  }

  private def fillEmptyResult(start: Date, end: Date): JsArray = {
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
        for(c <- payingUsers.value) {
          val payingUserIds = (c \ "payingUsers").as[List[JsValue]] map {el =>
            (el \ "userId").as[String]
          }
          users = (payingUserIds ++ users).distinct
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
    val revenueCollection = Metrics.totalRevenueCollection(companyName, applicationName)
    val activeUsersCollection = Metrics.activeUsersCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")

    val futureRevenue = databaseService.getDocumentsWithinTimeRange(
      revenueCollection,
      fields,
      start,
      end
    )
    val futureActiveUsers = databaseService.getDocumentsWithinTimeRange(
      activeUsersCollection,
      fields,
      start,
      end
    )

    for {
      revenue <- futureRevenue
      active <- futureActiveUsers
    } yield {
      if(active.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        new JsArray((revenue.value zip active.value) map {
          case (r, a) => {
            val day = new LocalDate(getDateFromString((r \ "lowerDate" \ "$date").as[String]))
            Json.obj(
              "day" -> day.toString("dd MM"),
              "value" -> (r \ "totalRevenue").as[Double] / (a \ "activeUsers").as[Int]
            )
          }
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
    val fields = ("lowerDate", "upperDate")
    val futureRevenue = databaseService.getDocumentsWithinTimeRange(
      Metrics.totalRevenueCollection(companyName, applicationName),
      fields,
      start,
      end
    )
    val futureActiveUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.activeUsersCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    for {
      revenue <- futureRevenue
      activeUsers <- futureActiveUsers
    } yield {
      val totalRevenue = revenue.value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      })

      val totalActiveUsers = activeUsers.value.foldLeft(0)((sum, obj) => {
        sum + (obj \ "activeUsers").as[Int]
      })

      Json.obj("value" -> (if(totalActiveUsers > 0) totalRevenue / totalActiveUsers else 0))
    }
  }

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val futureSessions = databaseService.getDocumentsWithinTimeRange(
      Metrics.numberSessionsCollection(companyName, applicationName),
      fields,
      start,
      end
    )
    val futureRevenue = databaseService.getDocumentsWithinTimeRange(
      Metrics.totalRevenueCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    for {
      sessions <- futureSessions
      revenue <- futureRevenue
    } yield {
      if(sessions.value.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        val s = new LocalDate(start)
        val days = Days.daysBetween(s, new LocalDate(end)).getDays()+1

        var coll = revenue.value zip sessions.value
        new JsArray((List.range(0, days)).map {d => {
          val _d = s.withFieldAdded(DurationFieldType.days(), d)
          val dailyValues = coll.filter({el: Tuple2[JsValue, JsValue] => {
            val day = _d.toDate
            val revenueLowerDate = getDateFromString((el._1 \ "lowerDate" \ "$date").as[String])
            val revenueUpperDate = getDateFromString((el._1 \ "upperDate" \ "$date").as[String])
            val sessionLowerDate = getDateFromString((el._2 \ "lowerDate" \ "$date").as[String])
            val sessionUpperDate = getDateFromString((el._2 \ "upperDate" \ "$date").as[String])
              (day.after(revenueLowerDate) && day.before(revenueUpperDate)) && (day.after(sessionLowerDate) && day.before(sessionUpperDate))
          }})
            var totalRevenue = 0.0
          var nrSessions = 0
          dailyValues.foreach {value: Tuple2[JsValue, JsValue] => {
            totalRevenue += (value._1 \ "totalRevenue").as[Double]
            nrSessions += (value._2 \ "totalSessions").as[Int]
          }}

          coll = coll.drop(dailyValues.size)
          Json.obj(
            "day" -> _d.toString("dd MMM"),
            "val" -> (if(nrSessions > 0) (totalRevenue / nrSessions) else 0)
          )
        }})
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
    val futureSessions = databaseService.getDocumentsByTimeRange(
      Metrics.mobileSessionsCollection(companyName, applicationName),
      "startTime",
      start,
      end
    )
    val futureRevenue = databaseService.getDocumentsWithinTimeRange(
      Metrics.totalRevenueCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    for {
      sessions <- futureSessions
      revenue <- futureRevenue
    } yield {
      val nrSessions = sessions.value.size
      val totalRevenue = revenue.value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      })

      Json.obj("value" -> (if(nrSessions > 0) totalRevenue / nrSessions else 0))
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
      Json.obj("value" -> revenue.value.foldLeft(0.0)((sum, obj) => {
        sum + (obj \ "totalRevenue").as[Double]
      }))
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
           val day = new LocalDate(getDateFromString((el \ "lowerDate" \ "$date").as[String])).toString("dd MM")
           Json.obj(
             "day" -> day,
             "val" -> (el \ "totalRevenue").as[Int]
           )
         }})
       }
     }
  }

  def getTotalChurnRate(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {

    val fields = ("lowerDate", "upperDate")
    val result = if(getNumberDaysBetweenDates(start, end) == 0) {
      val s = new DateTime(start).withTimeAtStartOfDay
      val yesterday = s.minusDays(1).withTimeAtStartOfDay
      val e = s.plusDays(1)
      val futureNumberPayingUsersLower = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        yesterday.toDate,
        s.toDate
      )
      val futureNumberPayingUsersUpper = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        s.toDate,
        e.toDate
      )
      for {
        numberPayingUsersLower <- futureNumberPayingUsersLower
        numberPayingUsersUpper <- futureNumberPayingUsersUpper
      } yield (numberPayingUsersLower, numberPayingUsersUpper)
    } else {
      val lower_1 = new DateTime(start).withTimeAtStartOfDay
      val lower = lower_1.plusDays(1).withTimeAtStartOfDay
      val upper_1 = new DateTime(end).withTimeAtStartOfDay
      val upper = upper_1.plusDays(1).withTimeAtStartOfDay
      val futureNumberPayingUsersLower = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        lower_1.toDate,
        lower.toDate
      )
      val futureNumberPayingUsersUpper = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        upper_1.toDate,
        upper.toDate
      )

      for {
        numberPayingUsersLower <- futureNumberPayingUsersLower
        numberPayingUsersUpper <- futureNumberPayingUsersUpper
      } yield (numberPayingUsersLower, numberPayingUsersUpper)
    }

    result map {dates =>
      val numberPayingUsersLower = dates._1
      val numberPayingUsersUpper = dates._2
      Json.obj("value" -> (if(numberPayingUsersLower > 0) {
        (numberPayingUsersUpper - numberPayingUsersLower) / numberPayingUsersLower
      } else 0))
    }
  }


  def getChurnRate(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalChurnRate)
  }

  def getTotalAverageNumberSessionsPerUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val futureSessionsPerUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.numberSessionsPerUserCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futureSessionsPerUser map {sessionsPerUser =>
      if(sessionsPerUser.value.isEmpty){
        Json.obj("value" -> 0)
      } else {
        var sessionUserMap: Map[String, Int] = Map()
        for(
          el <- sessionsPerUser.value;
          spuDay <- ((el \ "nrSessionsPerUser").as[List[JsValue]])
        ) {
          val userId = (spuDay \ "user").as[String]
          val nrSessions = (spuDay \ "nrSessions").as[Int]
          val value = sessionUserMap getOrElse(userId, 0)
          value match {
            case 0 => sessionUserMap += (userId ->  nrSessions)
            case _ => sessionUserMap += (userId -> (value + nrSessions))
          }
        }
        Json.obj(
          "value" -> (sessionUserMap.values.foldLeft(0.0)(_ + _) / sessionUserMap.values.size)
        )
      }
    }
  }

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val futureLTV = for {
      churn <- getTotalChurnRate(companyName, applicationName, start, end)
      arpu <- getTotalARPU(companyName, applicationName, start, end)
      avgSessionUser <- getTotalAverageNumberSessionsPerUser(companyName, applicationName, start, end)
    } yield {
      (1 - ((churn \ "value").as[Float])) *
      ((arpu \ "value").as[Float]) *
      ((avgSessionUser \ "value").as[Float]) *
      ProfitMargin
    }

    futureLTV map {ltv =>
      Json.obj("value" -> ltv)
    } recover {
      case ex: Exception => throw ex
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

def getTotalAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val futurePayingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    val futureSessionsPerUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.mobileSessionsCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    val result = for {
      payingUsers <- futurePayingUsers
      sessionsPerUser <- futureSessionsPerUser
    } yield {
      if(payingUsers.value.isEmpty) {
        Future.successful(Json.obj("value" -> 0))
      } else {
        val futurePurchaseTimes = Future.sequence(
          for(
            payingUsersDay <- payingUsers.value;
            userInfo <- ((payingUsersDay \ "payingUsers").as[List[JsValue]])
          ) yield {
            val userId = (userInfo \ "userId").as[String]
            val firstPurchase = (userInfo \ "purchases").as[List[String]].head
            val purchaseTime  = purchaseService.get(companyName, applicationName, firstPurchase) map {p =>
              getDateFromString((p map(_.time)).get)
            }
            val map: Map[String, Future[Date]] = Map(userId -> purchaseTime)
            Future.sequence(map.map(entry => entry._2.map(i => (entry._1, i)))).map(_.toMap)
          }
        )

        futurePurchaseTimes map {seq =>
          val timeFirstPurchasePerUser = seq.foldLeft(Map.empty[String, Date]){(map,element) =>
            if(!map.contains(element.keys.head)) {
              map += (element.keys.head -> element.values.head)
            } else map
          }
          if(sessionsPerUser.value.isEmpty){
           Json.obj("value" -> 0)
          } else {
            var totalTimeFirstPurchase = 0.0
            for(el <- sessionsPerUser.value) {
              val userId = (el \ "userId").as[String]
              if(timeFirstPurchasePerUser.contains(userId)){
                val firstSessionDate = getDateFromString((el \ "startTime").as[String])
                val firstPurchaseDate = getDateFromString((timeFirstPurchasePerUser.get(userId)).toString)
                totalTimeFirstPurchase += getNumberSecondsBetweenDates(firstSessionDate, firstPurchaseDate)
              }
            }
            val numberPurchases = timeFirstPurchasePerUser.size
            Json.obj("value" -> (if(numberPurchases == 0) 0 else totalTimeFirstPurchase / numberPurchases))        
          }
        }
      }
    }
  result flatMap {r => r}
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
    val futurePayingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    futurePayingUsers flatMap {payingUsers =>
      if(payingUsers.value.isEmpty) {
        Future.successful(Json.obj("value" -> 0))
      } else {
        val futurePurchasesTimes = Future.sequence(for(
          payingUsersDay <- payingUsers.value;
          userInfo <- ((payingUsersDay \ "payingUsers").as[List[JsValue]])
        ) yield {
          val userId = (userInfo \ "userId").as[String]
          val purchasesTime  = Future.sequence((userInfo \ "purchases").as[List[String]] map {id =>
            purchaseService.get(companyName, applicationName, id) map {p =>
              getDateFromString((p map(_.time)).get)
            }
          })

          val map: Map[String, Future[List[Date]]] = Map(userId -> purchasesTime)
          Future.sequence(map.map(entry => entry._2.map(i => (entry._1, i)))).map(_.toMap)
        })


        futurePurchasesTimes map {seq =>
          val purchaseTimesPerUser = seq.foldLeft(Map.empty[String, List[Date]]){(map,element) =>
            val purchases = map getOrElse(element.keys.head, Nil)
            purchases match {
              case Nil => map += (element.keys.head -> element.values.head)
              case _ => map += (element.keys.head -> (purchases ++ element.values.head))
            }
          }
          var numberPurchases = 0
          var totalTimeBetweenPurchases = 0.0
          for((u,t) <- purchaseTimesPerUser; times <- t.view.zipWithIndex) {
            val index = times._2
            val nrPurchases = t.size
            if(nrPurchases == 1) {
              numberPurchases += 1
            } else {
              if((index+1) < nrPurchases) {
                val currentPurchaseDate = times._1
                val nextPurchaseDate = t(index+1)
                totalTimeBetweenPurchases += getNumberSecondsBetweenDates(currentPurchaseDate, nextPurchaseDate)
              }
            }
          }
          Json.obj("value" -> (if(numberPurchases == 0) 0 else totalTimeBetweenPurchases / numberPurchases))
        }
      }
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
      applicationName,
      companyName,
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
    val futureSessions = databaseService.getDocumentsWithinTimeRange(
      Metrics.numberSessionsCollection(companyName, applicationName),
      ("lowerDate", "upperDate"),
      start,
      end
    )

    val futurePayingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      ("lowerDate", "upperDate"),
      start,
      end
    )

    for {
      sessions <- futureSessions
      payingUsers <- futurePayingUsers
    } yield {
      val nrSessions = if(sessions.value.isEmpty) {
        0
      } else {
        var res = 0
        for(s <- sessions.value) {
          res += sessions.value.foldLeft(0)((r,c) => r + (c \ "totalSessions").as[Int])
        }
        res
      }

      var nrPurchases = 0.0
      if(!payingUsers.value.isEmpty) {
        for(
          dailyInfo <- payingUsers.value;
          users <- ((dailyInfo \ "payingUsers").as[List[JsValue]])
        ) {
          nrPurchases += ((users \ "purchases").as[List[String]]).size
        }
      }
      Json.obj("value" -> (if(nrSessions > 0) (nrPurchases / nrSessions) else 0))
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
