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
  private lazy val PayPalProfitMargin = 0.029
  private val databaseProxy = PersistenceProxy.getInstance()
  private implicit val timeout = Timeout(8 seconds)

  private def fillEmptyResult(start: Date, end: Date, platforms: List[String], paymentSystems: List[Int]): JsArray = {
    val dates = new ListBuffer[String]()
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    new JsArray(List.range(0, days) map {i =>{
      Json.obj(
        "day" -> s.withFieldAdded(DurationFieldType.days(), i).toDate.getTime,
        "value" -> 0.0,
        "platforms" -> (platforms map {
          p => Json.obj(
            "value" -> 0.0,
            "platform" -> p,
            "paymentSystems" -> (paymentSystems map {s => Json.obj("system" -> s, "value" -> 0.0)})
          )
        })
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
    paymentSystems: List[Int],
    f:(String, String, Date, Date, List[String], List[Int]) => Future[JsValue]
  ): Future[JsArray] = {
    val s = new LocalDate(start)
    val e = new LocalDate(end)
    val days = Days.daysBetween(s, e).getDays() + 1

    val futureResult = Future.sequence(List.range(0, days) map {dayIndex =>
      val currentDay = s.withFieldAdded(DurationFieldType.days(), dayIndex)
      val previousDay = currentDay.minusDays(1)
      f(companyName, applicationName, previousDay.toDate, currentDay.toDate, platforms, paymentSystems) map {res: JsValue =>
        Json.obj(
          "day" -> currentDay.toDate.getTime,
          "value" -> (res \ "value").as[Double],
          "platforms" -> (platforms map {p => {
            getPlatform(res, p) match {
              case Some(platformInfo) => {
                Json.obj(
                  "platform" -> ((platformInfo \ "platform").as[String]),
                  "value" -> ((platformInfo \ "value").as[Double]),
                  "paymentSystems" -> ((platformInfo \ "paymentSystems").as[JsArray])
                )
              }
              case None => {
                Json.obj(
                  "platform" -> p,
                  "value" -> 0.0,
                  "paymentSystems" -> (paymentSystems map {s => Json.obj("system" -> s, "value" -> 0.0)})
                )
              }
            }
          }})
        )
      }
    })

    futureResult map {jsonSeq =>
      new JsArray(jsonSeq)
    }
  }

  // Gets list of platforms from JSON retrived from database
  private def getPlatform(json: JsValue, platform: String): Option[JsValue] = {
    (json \ "platforms").as[JsArray].value.find(e => (e \ "platform").as[String] == platform)
  }

  def getPaymentSystemResult(jsonArray: JsArray, system: Int): Double = {
    jsonArray.value.toList.find{s =>
      (s \ "system").as[Int] == system
    } match {
      case Some(data) => {
        if(data.as[JsObject].keys.contains("res")) {
          (data \ "res").as[Double]
        } else {
          (data \ "value").as[Double]
        }
      }
      case None => 0.0
    }
  }

  private def getDetailedResult(data: Seq[JsValue], platforms: List[String], paymentSystems: List[Int]): JsValue = {
    val emptyResult = Json.obj(
      "value" -> 0.0,
      "platforms" -> (platforms map {p =>
        Json.obj("platform" -> p, "value" -> 0.0, "paymentSystems" -> (paymentSystems map {s =>
          Json.obj("system" -> s, "value" -> 0.0)
        }))}
      )
    )
    data.foldLeft(emptyResult)((res, current) => {
      val updateResult = (res \ "value").as[Double] + (current \ "result").as[Double]
      val platformResults = platforms map {platform =>
        val resPlatform = getPlatform(res, platform).get
        val currentPlatformOpt = getPlatform(current, platform)
        currentPlatformOpt match {
          case Some(currentPlatform) => {
            val updatedValue = (resPlatform \ "value").as[Double] + (currentPlatform \ "res").as[Double]
            val paymentSystemsResults = paymentSystems map {system =>
              val current = getPaymentSystemResult((currentPlatform \ "paymentSystems").as[JsArray], system)
              val res = getPaymentSystemResult((resPlatform \ "paymentSystems").as[JsArray], system)
              Json.obj("system" -> system, "value" -> (current + res))
            }
            Json.obj("platform" -> platform, "value" -> updatedValue, "paymentSystems" -> paymentSystemsResults)
          }
          case _ => resPlatform
        }
      }
      Json.obj("value" -> updateResult, "platforms" -> platformResults)
    })
  }

  private def getAverageOfTotalResults(
    data: Seq[JsValue],
    platforms: List[String],
    paymentSystems: List[Int],
    start: Date,
    end: Date
  ): JsValue = {
    def averagenizer(v: Double): Double = {
      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      if(days > 0) v / days else v
    }

    def averagenizerPaymentSystems(jsonArray: JsArray, system: Int): Double = {
      (jsonArray.value.toList.find(s => (s \ "system").as[Int] == system).get \ "value").as[Double]
    }

    val res = getDetailedResult(data, platforms, paymentSystems)
    Json.obj(
      "value" -> averagenizer((res \ "value").as[Double]),
      "platforms" -> ((res \ "platforms").as[JsArray].value map {p =>
        Json.obj(
          "platform" -> ((p \ "platform").as[String]),
          "value" -> averagenizer((p \ "value").as[Double]),
          "paymentSystems" -> (paymentSystems map {system =>
            Json.obj(
              "system" -> system,
              "value" -> averagenizer(averagenizerPaymentSystems((p \ "paymentSystems").as[JsArray], system))
            )
          })
        )
      })
    )
  }

  def getARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalARPU)
  }

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val arpuCollection = Metrics.arpuCollection(companyName, applicationName)
    val fields = ("lowerDate", "upperDate")
    val request = new GetDocumentsWithinTimeRange(new Stack, arpuCollection, fields, start, end, true)
    val futureArpu = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureArpu map {r =>
      getAverageOfTotalResults(r.res.value, platforms, paymentSystems, start, end)
    }
  }

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalAverageRevenuePerSession)
  }

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgRevenueSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureAvgRevenueSession = (databaseProxy ? request).mapTo[PRJsArrayResponse]


    def averagenizer(v: Double): Double = {
      val days = DateUtils.getNumberDaysBetweenDates(start, end)
      if(days > 0) v / days else v
    }

    def averagenizerPaymentSystems(jsonArray: JsArray, system: Int): Double = {
      (jsonArray.value.toList.find(s => (s \ "system").as[Int] == system).get \ "value").as[Double]
    }

    futureAvgRevenueSession map {r =>
      val res = getDetailedResult(r.res.value, platforms, paymentSystems)
      Json.obj(
        "value" -> averagenizer((res \ "value").as[Double]),
        "platforms" -> ((res \ "platforms").as[JsArray].value map {p =>
          Json.obj(
            "platform" -> ((p \ "platform").as[String]),
            "value" -> averagenizer((p \ "value").as[Double]),
            "paymentSystems" -> (paymentSystems map {system =>
              Json.obj(
                "system" -> system,
                "value" -> averagenizer(averagenizerPaymentSystems((p \ "paymentSystems").as[JsArray], system))
              )
            })
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
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.totalRevenueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRevenue = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    futureRevenue map {r => getDetailedResult(r.res.value, platforms, paymentSystems)}
  }

  def getRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalRevenue)
  }

  def getTotalAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.avgPurchasesUserCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {r =>
      getAverageOfTotalResults(r.res.value, platforms, paymentSystems, start, end)
    }
  }

  def getAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalAveragePurchasesUser)
  }

  def getTotalAverageNumberSessionsPerUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
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
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.lifeTimeValueCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {ltv =>
      getAverageOfTotalResults(ltv.res.value, platforms, paymentSystems, start, end)
    }
  }

  def getLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalLifeTimeValue)
  }

  def getTotalNumberSessionsFirstPurchase(
    companyName: String, 
    applicationName: String, 
    start: Date, 
    end: Date, 
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.sessionsFirstPurchase(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRes = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureRes map {result =>
      val emptyRes = (0.0, platforms map{(_, 0.0, paymentSystems map {(_, 0.0)})})
      val res = result.res.value.foldLeft(emptyRes)((acc, current) => {
        val sessions = acc._1 + (current \ "result").as[Double]
        val pInfo = (current \ "platforms").as[JsArray].value.toList
        val platformData = acc._2 map {p =>
          pInfo.find(pp => (pp \ "platform").as[String] == p._1) match {
            case Some(info) => {
              val systemsData = (info \ "paymentSystems").as[JsArray].value.toList
              val paymentSystemsResults = paymentSystems map {s =>
                val current = systemsData.find(e => (e \ "system").as[Int] == s)
                  .map(e => (e \ "result").as[Double])
                  .getOrElse(0.0)
                val accum = p._3.find(_._1 == s)
                  .map(_._2)
                  .getOrElse(0.0)
                (s, current + accum)
              }
              (p._1, p._2 + (info \ "result").as[Double], paymentSystemsResults)
            }
            case None => (p._1, p._2, p._3)
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
            "value" -> averagenizer(p._2),
            "paymentSystems" -> (p._3 map {s =>
              Json.obj("system" -> s._1, "value" -> averagenizer(s._2))
            })
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
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalNumberSessionsFirstPurchase)
  }

  def getTotalNumberSessionsBetweenPurchases(
    companyName: String, 
    applicationName: String, 
    start: Date, 
    end: Date, 
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.sessionsBetweenPurchasesCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val futureRes = (databaseProxy ? request).mapTo[PRJsArrayResponse]

    futureRes map {result =>
      val emptyRes = (0.0, platforms map{(_, 0.0, paymentSystems map {(_, 0.0)})})
      val res = result.res.value.foldLeft(emptyRes)((acc, current) => {
        val sessions = acc._1 + (current \ "result").as[Double]
        val pInfo = (current \ "platforms").as[JsArray].value.toList
        val platformData = acc._2 map {p =>
          pInfo.find(pp => (pp \ "platform").as[String] == p._1) match {
            case Some(info) => {
              val systemsData = (info \ "paymentSystems").as[JsArray].value.toList
              val paymentSystemsResults = paymentSystems map {s =>
                val current = systemsData.find(e => (e \ "system").as[Int] == s)
                  .map(e => (e \ "res").as[Double])
                  .getOrElse(0.0)
                val accum = p._3.find(_._1 == s)
                  .map(_._2)
                  .getOrElse(0.0)
                (s, current + accum)
              }
              (p._1, p._2 + (info \ "res").as[Double], paymentSystemsResults)
            }
            case None => (p._1, p._2, p._3)
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
            "value" -> averagenizer(p._2),
            "paymentSystems" -> (p._3 map {s =>
              Json.obj("system" -> s._1, "value" -> averagenizer(s._2))
            })
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
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalNumberSessionsBetweenPurchases)
  }

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val collection = Metrics.payingUsersCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, ("lowerDate", "upperDate"), start, end, true)
    val empty = Json.obj(
      "value" -> 0,
      "platforms" -> (platforms map {p =>
        Json.obj("platform" -> p,
          "value" -> 0,
          "paymentSystems" -> (paymentSystems map {s => Json.obj("system" -> s, "value" -> 0)})
        )
      })
    )
    (databaseProxy ? request).mapTo[PRJsArrayResponse] map {r =>
      if(r.res.value.isEmpty) {
        empty
      } else {
        r.res.value.foldLeft(empty){(res, current) => {
          val totalUpdated = (res \ "value").as[Int] + 1
          val updatedPlatforms = platforms map {platform =>
            val pInfo = (res \ "platforms").as[JsArray].value.find(p => (p \ "platform").as[String] == platform).get
            val optPlatform =  (current \ "purchasesPerPlatform").as[JsArray].value.find(p =>
              (p \ "platform").as[String] == platform
            )
            val newValue = optPlatform match {
              case Some(p) => (pInfo \ "value").as[Int] + 1
              case None => (pInfo \ "value").as[Int]
            }
            val paymentSystemInfo = paymentSystems map {system =>
              // To sum one: check if platform exists, then check if payment system exists
              optPlatform match {
                case Some(p) => {
                  (p \ "purchases").as[JsArray].value.toList.find(s => (s \ "paymentSystem").as[Double].toInt == system) match {
                    case Some(paymentSystemInfo) => {
                      val value = (pInfo \ "paymentSystems").as[JsArray].value.toList.find(s => (s \ "system").as[Double].toInt == system).get
                      Json.obj("system" -> system, "value" -> ( (value \ "value").as[Int] + 1))
                    }
                    case None => (pInfo \ "paymentSystems").as[JsArray].value.toList.find(s => (s \ "system").as[Double].toInt == system).get
                  }
                }
                case None => (pInfo \ "paymentSystems").as[JsArray].value.toList.find(s => (s \ "system").as[Double].toInt == system).get
              }
            }
            Json.obj("platform" -> platform, "value" -> newValue, "paymentSystems" -> paymentSystemInfo)
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
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalNumberPayingCustomers)
  }

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsValue] = {
    val fields = ("lowerDate", "upperDate")
    val collection = Metrics.averagePurchasePerSessionCollection(companyName, applicationName)
    val request = new GetDocumentsWithinTimeRange(new Stack, collection, fields, start, end, true)
    val future = (databaseProxy ? request).mapTo[PRJsArrayResponse]
    future map {p =>
      getAverageOfTotalResults(p.res.value, platforms, paymentSystems, start, end)
    }
  }

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String],
    paymentSystems: List[Int]
  ): Future[JsArray] = {
    calculateDetailedKPIAux(companyName, applicationName, start, end, platforms, paymentSystems, getTotalAveragePurchasePerSession)
  }
}

