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
  ): Float = {
    val collection = Metrics.payingUsersCollection(companyName, applicationName)
    val payingUsers = databaseService.getDocumentsWithinTimeRange(collection, fields, s, e)
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
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalARPU)
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
    Future {
      val fields = ("lowerDate", "upperDate")
      val sessions = databaseService.getDocumentsWithinTimeRange(
        Metrics.numberSessionsCollection(companyName, applicationName),
        fields,
        start,
        end
      ).value

      val revenue = databaseService.getDocumentsWithinTimeRange(
        Metrics.totalRevenueCollection(companyName, applicationName),
        fields,
        start,
        end
      ).value

      val result = if(sessions.isEmpty) {
        fillEmptyResult(start, end)
      } else {
        val s = new LocalDate(start)
        val days = Days.daysBetween(s, new LocalDate(end)).getDays()+1

        var coll = revenue zip sessions
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
      promise.success(result)
    }
    promise.future
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
    Future {
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
    }

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
    calculateDetailedKPIAux(companyName, applicationName, start, end, getTotalRevenue)
  }

  def getTotalChurnRate(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {

    val fields = ("lowerDate", "upperDate")
    var numberPayingUsersLower = 0.0
    var numberPayingUsersUpper = 0.0

    val promise = Promise[JsValue]
    if(getNumberDaysBetweenDates(start, end) == 0) {
      val s = new DateTime(start).withTimeAtStartOfDay
      val yesterday = s.minusDays(1).withTimeAtStartOfDay
      val e = s.plusDays(1)
      numberPayingUsersLower = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        yesterday.toDate,
        s.toDate
      )
      numberPayingUsersUpper = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        s.toDate,
        e.toDate
      )
    } else {
      val lower_1 = new DateTime(start).withTimeAtStartOfDay
      val lower = lower_1.plusDays(1).withTimeAtStartOfDay
      val upper_1 = new DateTime(end).withTimeAtStartOfDay
      val upper = upper_1.plusDays(1).withTimeAtStartOfDay
      numberPayingUsersLower = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        lower_1.toDate,
        lower.toDate
      )
      numberPayingUsersUpper = calculateNumberPayingCustomers(
        companyName,
        applicationName,
        fields,
        upper_1.toDate,
        upper.toDate
      )
    }

    val result = if(numberPayingUsersLower > 0) {
      (numberPayingUsersUpper - numberPayingUsersLower) / numberPayingUsersLower
    } else 0
    promise.success(Json.obj("value"-> result))
    promise.future
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
    val promise = Promise[JsValue]
    val fields = ("lowerDate", "upperDate")
    val sessionsPerUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.numberSessionsPerUserCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    if(sessionsPerUser.value.isEmpty){
      promise.success(Json.obj("value" -> 0))
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
      promise.success(Json.obj(
        "value" -> (sessionUserMap.values.foldLeft(0.0)(_ + _) / sessionUserMap.values.size)
      ))
    }
    promise.future
  }

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue] = {
    val promise = Promise[JsValue]
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
      promise.success(Json.obj("value" -> ltv))
    } recover {
      case ex: Exception => promise.failure(ex)
    }
    promise.future
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
    val promise = Promise[JsValue]
    val fields = ("lowerDate", "upperDate")
    val payingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    val sessionsPerUser = databaseService.getDocumentsWithinTimeRange(
      Metrics.mobileSessionsCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    var totalTimeFirstPurchase = 0.0
    var purchaseTimesPerUser: Map[String, Date] = Map()

    if(payingUsers.value.isEmpty) {
      promise.success(Json.obj("value" -> 0))
    } else {
      for(
        payingUsersDay <- payingUsers.value;
        userInfo <- ((payingUsersDay \ "payingUsers").as[List[JsValue]])
      ) {
        val userId = (userInfo \ "userId").as[String]
        if(!purchaseTimesPerUser.contains(userId)){
          val purchaseTime  = (userInfo \ "purchases").as[List[String]] map {id =>
            getDateFromString(purchaseService.get(companyName, applicationName, id).get.time)
          }
          purchaseTimesPerUser += (userId -> purchaseTime.head)
        }
      }
    }

    if(sessionsPerUser.value.isEmpty){
      promise.success(Json.obj("value" -> 0))
    } else {
      for(
        el <- sessionsPerUser.value;
        userId <- ((el \ "user").as[String])
      ) {
        if(purchaseTimesPerUser.contains(userId)){
          val firstSessionDate = getDateFromString((el \ "startTime").as[String])
          val firstPurchaseDate = getDateFromString(purchaseTimesPerUser.get(userId).as[String])
          totalTimeFirstPurchase += getNumberSecondsBetweenDates(firstSessionDate, firstPurchaseDate)
        }
      }

      val numberPurchases = purchaseTimesPerUser.size

      promise.success(
        Json.obj("value" -> (if(numberPurchases == 0) 0 else totalTimeFirstPurchase / numberPurchases))
      )
    }

    promise.future
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
    val promise = Promise[JsValue]
    val fields = ("lowerDate", "upperDate")
    val payingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      fields,
      start,
      end
    )

    if(payingUsers.value.isEmpty) {
      promise.success(Json.obj("value" -> 0))
    } else {
      var totalTimeBetweenPurchases = 0.0
      var numberPurchases = 0
      var purchaseTimesPerUser: Map[String, List[Date]] = Map()
      for(
        payingUsersDay <- payingUsers.value;
        userInfo <- ((payingUsersDay \ "payingUsers").as[List[JsValue]])
      ) {
        val userId = (userInfo \ "userId").as[String]
        val purchasesTime  = (userInfo \ "purchases").as[List[String]] map {id =>
          getDateFromString(purchaseService.get(companyName, applicationName, id).get.time)
        }

        val purchases = purchaseTimesPerUser getOrElse(userId, Nil)
        purchases match {
          case Nil => purchaseTimesPerUser += (userId -> purchasesTime)
          case _ => purchaseTimesPerUser += (userId -> (purchases ++ purchasesTime))
        }
      }

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
      promise.success(
        Json.obj("value" -> (if(numberPurchases == 0) 0 else totalTimeBetweenPurchases / numberPurchases))
      )
    }

    promise.future
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
    val promise = Promise[JsValue]
    promise.success(Json.obj("value" -> calculateNumberPayingCustomers(
      applicationName,
      companyName,
      ("lowerDate", "upperDate"),
      start,
      end
    )))

    promise.future
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
    val promise = Promise[JsValue]

    val sessions = databaseService.getDocumentsWithinTimeRange(
      Metrics.numberSessionsCollection(companyName, applicationName),
      ("lowerDate", "upperDate"),
      start,
      end
    )

    val nrSessions = if(sessions.value.isEmpty) {
      0
    } else {
      var res = 0
      for(s <- sessions.value) {
        res += sessions.value.foldLeft(0)((r,c) => r + (c \ "totalSessions").as[Int])
      }
      res
    }

    val payingUsers = databaseService.getDocumentsWithinTimeRange(
      Metrics.payingUsersCollection(companyName, applicationName),
      ("lowerDate", "upperDate"),
      start,
      end
    )

    var nrPurchases = 0.0
    if(!payingUsers.value.isEmpty) {
      for(
        dailyInfo <- payingUsers.value;
        users <- ((dailyInfo \ "payingUsers").as[List[JsValue]])
      ) {
        nrPurchases += ((users \ "purchases").as[List[String]]).size
      }
    }

    promise.success(Json.obj("value" -> (if(nrSessions > 0) (nrPurchases / nrSessions) else 0)))
    promise.future
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
