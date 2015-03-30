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
import models.common._
import models.payments._

class BootstrapEnvironmentController extends Controller {

  private var dates = List[Date]()
  private var DEFAULT_DAYS = 7
  private var NumberMobileUsers = 0
  private val LowerPrice = 1.99
  private val UpperPrice = 5.99
  private val NumberPurchases = 70
  private val NumberItems = 10

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
    dates map {currentDay =>
      (1 to NumberMobileUsers) map {userNumber =>
        val platform = if(Math.random() > 0.5) platforms.head else platforms.last
        val session = new MobileSession(
          (s"${currentDay.toString}-$userNumber"), //hash
          userNumber.toString,
          2,
          currentDay,
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
    dates map {currentDay =>
      (1 to NumberMobileUsers) map {userNumber =>
        val makePurchases = willMakePurchases
        if(makePurchases) {
          val itemsAux = items
          val item = Random.shuffle(itemsAux).head
          val platform = if(Math.random() > 0.5) platforms.head else platforms.last
          // val purchaseInfo = new PurchaseInfo(
          //   s"purchase-$userNumber-${currentDay.toString}",
          //   (s"${currentDay.toString}-$userNumber"),
          //   userNumber.toString,
          //   item._1,
          //   item._2,
          //   currentDay,
          //   new DeviceInfo(platform, "name", "version", "model"),
          //   None,
          //   null //TODO
          // )
          // val request = new PRSave(new Stack, companyName, applicationName, purchaseInfo)
          // userProxy ! request
        }
      }
    }
  }
  
  def execute(companyName: String, applicationName: String) = Action.async {
    val platforms = List("iOS", "Android")
    println("company: " + companyName + " | app: " + applicationName + " | platforms: " + platforms)
    val first = new LocalDate(new Date).withDayOfMonth(1)
    val days = DEFAULT_DAYS
    dates = List.range(0, days) map {index =>
      first.withFieldAdded(DurationFieldType.days(), index).toDate
    }
    NumberMobileUsers = 10
    addUser(companyName)
    addApplication(companyName, applicationName, "me@mail.com")
    createSessions(companyName, applicationName, platforms)
    Thread.sleep(8000);
    createPurchases(companyName, applicationName, platforms)
    Future.successful(Ok)
  }
}

