package controllers.api

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import models.application._
import service.user.definitions._
import com.google.inject._
import scala.math.BigDecimal
import models.user._
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.DurationFieldType
import org.joda.time.DateTime
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.util.Random
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import user._
import user.messages._
import application._
import application.messages._
import models.application._
import models.user._
import scala.collection.mutable.Stack
import akka.actor._
import persistence._
import notifications._

class BootstrapEnvironmentController extends Controller {

  private lazy val LowerPrice = 1.99
  private lazy val UpperPrice = 5.99
  private lazy val NumberMobileUsers = 70
  private lazy val NumberPurchases = 70
  private lazy val NumberItems = 10

  private object ApplicationData {
    val appUrl = "www.example.com"
    val imageName = "image-test"
    val packageName = "com.example"
    val appType = WazzaApplication.applicationTypes.last
    val credentials = new Credentials("id", "token")
    val items = List[Item]()
    val virtualCurrencies = List[VirtualCurrency]()
  }

  private val system = ActorSystem("Bootstrap")
  private val persistenceProxy = PersistenceProxy.getInstance(system)
  private val notificationsProxy = NotificationsProxy.getInstance(system)
  private val userProxy = UserProxy.getInstance(system)
  private val appProxy = ApplicationProxy.getInstance(system)

  private def addUser(companyName: String) = {
    println("Adding new user")
    val user = new User("userName", "me@mail.com", "1", companyName, List[String]())
    userProxy ! new URInsert(new Stack, user, true)
  }

  private def addApplication(companyName: String, applicationName: String, email: String) = {
    println("Adding new application")
    val application = new WazzaApplication(
      applicationName,
      ApplicationData.appUrl,
      ApplicationData.imageName,
      ApplicationData.packageName,
      List(ApplicationData.appType),
      ApplicationData.credentials,
      ApplicationData.items,
      ApplicationData.virtualCurrencies
    )
    val appInsertRequest = new ARInsert(new Stack, companyName, application, true)
    appProxy ! appInsertRequest
    val userAddAppRequest = new URAddApplication(new Stack, email, application.name, true)
    userProxy ! userAddAppRequest
  }

  private def createSessions(companyName: String, applicationName: String, platforms: List[String]) = {
    val end = new LocalDate()
    val start = end.minusDays(7)
    val days = Days.daysBetween(start, end).getDays()+1
    println(s"START $start | END $end")
    println(days)
    val result = List.range(0, days) map {index =>
      val currentDay = start.withFieldAdded(DurationFieldType.days(), index)
      println(s"CURRENT DAY $currentDay")
      (1 to NumberMobileUsers) map {userNumber =>
        val platform = if(Math.random() > 0.5) platforms.head else platforms.last
        val session = new MobileSession(
          (s"${currentDay.toString}-$userNumber"), //hash
          userNumber.toString,
          2,
          currentDay.toDate,
          new DeviceInfo(platform, "name", "version", "model"),
          List[String]() //List of purchases id's
        )

        val req = new SRSave(new Stack, companyName, applicationName, session, true)
        userProxy ! req
      }
    }
  }

  private def createPurchases(companyName: String, applicationName: String, platforms: List[String]) = {
    def generateItems(numberItems: Int): List[(String, Double)] = {
      def generateItemPrice(lowerPrice: Double, upperPrice: Double): Double = {
        lazy val DecimalPlaces = 2
        val price = (Math.random() * (upperPrice - lowerPrice)) + lowerPrice
        BigDecimal(price).setScale(DecimalPlaces, BigDecimal.RoundingMode.HALF_UP).toDouble
      }
        (1 to numberItems).map {i=>
          (s"name-$i", generateItemPrice(LowerPrice, UpperPrice))
        }.toList
    }

    def willMakePurchases = if(Math.random() > 0.5) true else false
    val items = generateItems(NumberItems)
    val end = new LocalDate()
    val start = end.minusDays(7)
    val days = Days.daysBetween(start, end).getDays()+1

    println(s"START $start | END $end")
    println(days)
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
    val result = List.range(0, days) map {index =>
      val currentDay = start.withFieldAdded(DurationFieldType.days(), index)
      (1 to NumberMobileUsers) map {userNumber =>
        val makePurchases = willMakePurchases
        if(makePurchases) {
          val itemsAux = items
          val item = Random.shuffle(itemsAux).head
          val platform = if(Math.random() > 0.5) platforms.head else platforms.last
          val purchaseInfo = new PurchaseInfo(
            s"purchase-$userNumber-${currentDay.toString}",
            (s"${currentDay.toString}-$userNumber"),
            userNumber.toString,
            item._1,
            item._2,
            currentDay.toDate,
            new DeviceInfo(platform, "name", "version", "model"),
            None
          )
          val request = new PRSave(new Stack, companyName, applicationName, purchaseInfo)
          userProxy ! request
        }
      }
    }
  }
  
  def execute(companyName: String, applicationName: String, platformsOption: Boolean = true) = Action.async {
    val platforms = List("iOS", "Android")
    println("company: " + companyName + " | app: " + applicationName + " | platforms: " + platforms)
    //addUser(companyName)
    addApplication(companyName, applicationName, "me@mail.com")
    createSessions(companyName, applicationName, platforms)
    createPurchases(companyName, applicationName, platforms)
    Future.successful(Ok)
  }
}

