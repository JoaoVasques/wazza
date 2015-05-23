package io.wazza.bootstrap

import java.util.Date
import java.util.Random
import java.util.TimeZone
import java.math._
import java.time._
import collection.JavaConversions._
import com.mongodb.casbah.Imports._
import scala.collection.mutable.ListBuffer

case class Info(max: Double, isInteger: Boolean, sums: Boolean)

object Bootstrap {

  private val Random = new Random

  /** Models **/

  def PaymentSystemsResult(system: Int, res: Double): MongoDBObject = {
    MongoDBObject("system" -> system, "res" -> res)
  }

  def PlatformResults(platform: String, res: Double, paymentSystemsResults: List[MongoDBObject]): MongoDBObject = {
    MongoDBObject(
      "platform" -> platform,
      "res" -> res,
      "paymentSystems" -> paymentSystemsResults
    )
  }

  def ThoRResult(
    beginDate: Date,
    endDate: Date,
    totalResult: Double,
    androidResults: MongoDBObject,
    iOSResults: MongoDBObject
  ): MongoDBObject = {
    MongoDBObject(
      "result" -> totalResult,
      "platforms" -> List(androidResults, iOSResults),
      "lowerDate" -> beginDate,
      "upperDate" -> endDate
    )
  }

  def purchasesPerPlatform(platform: String): MongoDBObject = {
    MongoDBObject(
      "platform" -> platform,
      "purchases" -> List[MongoDBObject]()
    )
  }

  def purchaseInfo(id: Int, time: Date): MongoDBObject = {
    MongoDBObject(
      "purchaseId" -> id,
      "time" -> time,
      "paymentSystem" ->(Math.round(Random.nextDouble * 2) + 1).toInt,
      "platform" -> {
        if(Random.nextDouble() > 0.5) "Android" else "iOS"
      }
    )
  }

  def payingUsers(userId: Int, beginDate: Date, endDate: Date, nrPurchases: Int): MongoDBObject = {

    var purchases = new ListBuffer[MongoDBObject]()
    var purchasesPlatform = new ListBuffer[MongoDBObject]()
    purchasesPlatform += purchasesPerPlatform("Android")
    purchasesPlatform += purchasesPerPlatform("iOS")

    (0 to nrPurchases).foreach{p =>
      val info = purchaseInfo(p, beginDate)
      purchases += info
      if(info.getAs[String]("platform") == "Android") {
        val lst = purchasesPlatform.head.getAs[List[MongoDBObject]]("purchases").get
        purchasesPlatform.head.put("purchases", lst :+ info)
      } else {
        val lst = purchasesPlatform.last.getAs[List[MongoDBObject]]("purchases").get
        purchasesPlatform.head.put("purchases", lst :+ info)
      }
    }

    MongoDBObject(
      "userId" -> userId,
      "purchases" -> purchases.toList,
      "purchasesPerPlatform" -> purchasesPlatform.toList,
      "lowerDate" -> beginDate,
      "upperDate" -> endDate
    )
  }

  def NumberSessionsFirstPurchasePerPlatform(nrSessions: Int, platform: String): MongoDBObject = {
    MongoDBObject(
      "platform" -> platform,
      "result" -> nrSessions,
      "nrUsers" -> 0.0,
      "paymentSystems" -> List(
        MongoDBObject("system" -> 1, "result" -> nrSessions, "nrUsers" -> 0.0),
        MongoDBObject("system" -> 2, "result" -> nrSessions, "nrUsers" -> 0.0)
      )
    )
  }

  def NumberSessionsFirstPurchase(nrSessions: Int, beginDate: Date, endDate: Date): MongoDBObject = {
    MongoDBObject(
      "result" -> nrSessions,
      "nrUsers" -> 0.0,
      "platforms" -> List(
        NumberSessionsFirstPurchasePerPlatform(nrSessions, "Android"),
        NumberSessionsFirstPurchasePerPlatform(nrSessions, "iOS")
      ),
      "lowerDate" -> beginDate,
      "upperDate" -> endDate
    )
  }

  /** Aux Functions **/

  def generateResults(upperBound: Int, isInteger: Boolean, sums: Boolean = false, totalValue: Double = 0.0) = {
    val random = new Random
    if(sums) {
      if(isInteger) {
        Math.round(random.nextDouble() * totalValue)
      } else {
        random.nextDouble() * totalValue
      }
    } else {
      if(isInteger) {
        Math.round(random.nextDouble() * upperBound)
      } else {
        random.nextDouble() * upperBound
      }
    }
  }

  def generateDates(numberDays: Int): List[Date] = {
    ((0 to numberDays) map {day: Int => {
      val d = new Date
      val instant = Instant.ofEpochMilli(d.getTime)
      Date.from(LocalDateTime.ofInstant(instant, ZoneId.systemDefault).minusDays(day).atZone(ZoneId.systemDefault()).toInstant())
    }}).toList
  }

  def saveToDB(result: MongoDBObject, index: Int) = {
    val uri = MongoClientURI("mongodb://localhost:27017/dev")
    val client = MongoClient(uri)

    /**
      val uri  = MongoClientURI(uriStr)
      val client = MongoClient(uri)
      val collection = client.getDB(uri.database.get).getCollection(collectionName)
      collection.insert(resultsToMongo(results))
      client.close()
      * */

    val collectionName = index match {
      case 0 => "Wazza_TotalRevenue_Demo"
      case 1 => "Wazza_Arpu_Demo"
      case 2 => "Wazza_avgRevenueSession_Demo"
      case 3 => "Wazza_LifeTimeValue_Demo"
      case 4 => "Wazza_payingUsers_Demo"
      case 5 => "Wazza_avgPurchasesUser_Demo"
      case 6 => "Wazza_PurchasesPerSession_Demo"
      case 7 => "Wazza_NumberSessionsFirstPurchase_Demo"
      case 8 => "Wazza_NumberSessionsBetweenPurchases_Demo"
    }
    val collection = client.getDB(uri.database.get).getCollection(collectionName)
    collection.insert(result)
    client.close()
  }

  /** Constants **/
  val NUMBER_DAYS = 10;
  
  val MAX_REVENUE = 2500;
  val MAX_ARPU = 3;
  val MAX_AVG_REVENUE_SESSION = 3;

  val MAX_LIFE_TIME_VALUE = 2;
  val MAX_PAYING_USERS = 300;
  val MAX_PURCHASES_PER_USER = 10;
  val MAX_AVG_PURCHASES_USER = 2;

  val MAX_PURCHASES_SESSION = 2;
  val MAX_SESSIONS_TO_FIRST_PURCHASE = 4;
  val MAX_SESSIONS_BETWEEN_PURCHASES = 4;

  var info = ListBuffer[Info]()
  info += new Info(MAX_REVENUE, false, true)
  info += new Info(MAX_ARPU, false, false)
  info += new Info(MAX_AVG_REVENUE_SESSION, false, false)

  info += new Info(MAX_LIFE_TIME_VALUE, false, false)
  info += new Info(MAX_PAYING_USERS, true, true)
  info += new Info(MAX_AVG_PURCHASES_USER, false, false)

  info += new Info(MAX_PURCHASES_SESSION, false, false)
  info += new Info(MAX_SESSIONS_TO_FIRST_PURCHASE, false, false)
  info += new Info(MAX_SESSIONS_BETWEEN_PURCHASES, false, false)

  def main(args: Array[String]) {
    println("TIMEONZE: " + TimeZone.getDefault())
    val dates = generateDates(NUMBER_DAYS)
    dates.foreach(println)

    for(date <- dates.zipWithIndex) {
      if((date._2 + 1) < dates.length) {
        println("CURRENT DATE: " + date._1)
        for(infoElement <- info.zipWithIndex) {
          infoElement._2 match {
            case 4 => {
              val nrUsers = generateResults(infoElement._1.max.toInt, true).toInt
              (0 to nrUsers).foreach{j =>
                val nrPurchases = generateResults(MAX_PURCHASES_PER_USER, infoElement._1.isInteger);
                val result = payingUsers(j, dates.get(date._2 + 1), date._1, nrPurchases.toInt)
                saveToDB(result, infoElement._2)
              }
            }
            case 7 => {
              val sessions = generateResults(infoElement._1.max.toInt, true).toInt
              val result = NumberSessionsFirstPurchase(sessions, dates.get(date._2 + 1), date._1)
              saveToDB(result, infoElement._2)
            }
            case _ => {
              val total = generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, infoElement._1.max)

              /** Android Data **/
              val androidResult = generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, total)
              val firstPaymentResultValue = generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, androidResult)
              val firstPaymentResult = PaymentSystemsResult(1, firstPaymentResultValue)
              val android = PlatformResults(
                "Android",
                androidResult,
                List(firstPaymentResult,
                  PaymentSystemsResult(2, generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, firstPaymentResultValue))
                )
              )

              /** iOS data **/
              val iOSResult = generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, androidResult)
              val firstiOSPaymentResultValue = generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, iOSResult)
              val firstiOSPaymentResult = PaymentSystemsResult(1, firstiOSPaymentResultValue)
              val iOS = PlatformResults(
                "iOS",
                iOSResult,
                List(firstiOSPaymentResult,
                  PaymentSystemsResult(2, generateResults(infoElement._1.max.toInt, infoElement._1.isInteger, infoElement._1.sums, firstiOSPaymentResultValue))
                )
              )

              val result = ThoRResult(dates.get(date._2 + 1), date._1, total, android, iOS)
              saveToDB(result, infoElement._2)
            }
          }
        }
      }
    }
  }
}

