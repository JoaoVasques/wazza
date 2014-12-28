/**package controllers.api

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import models.application._
import service.application.definitions._
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

class BootstrapEnvironmentController @Inject()(
  applicationService: ApplicationService,
  userService: UserService,
  purchaseService: PurchaseService,
  mobileSessionService: MobileSessionService
) extends Controller {

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

  private def generateItems(numberItems: Int): List[(String, Double)] = {

    def generateItemPrice(lowerPrice: Double, upperPrice: Double): Double = {
      lazy val DecimalPlaces = 2
      val price = (Math.random() * (upperPrice - lowerPrice)) + lowerPrice
      BigDecimal(price).setScale(DecimalPlaces, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
    (1 to numberItems).map {i=>
      (s"name-$i", generateItemPrice(LowerPrice, UpperPrice))
    }.toList
  }

  private def generateSessions(companyName: String, applicationName: String): Future[Boolean] = {

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
      println(s"CURRENT DAY $currentDay")
      Future.sequence((1 to NumberMobileUsers) map {userNumber =>
        val session = MobileSession(
          (currentDay.toString + userNumber), //hash
          userNumber.toString,
          2,
          currentDay.toDate,
          new DeviceInfo("osType", "name", "version", "model"),
          List[String]() //List of purchases id's
        )
        //println(session)
        mobileSessionService.insert(companyName, applicationName, session)/** flatMap {r =>
          val makePurchases = willMakePurchases
          //println(makePurchases)
          if(makePurchases) {
            val itemsAux = items
            val item = Random.shuffle(itemsAux).head
            //println(item)
            val date = format.format(currentDay.toDate)
            //println(date)
            val purchaseInfo = new PurchaseInfo(
              s"purchase-$userNumber-$currentDay.toString",
              (currentDay.toString + userNumber),
              userNumber.toString,
              applicationName,
              item._1,
              item._2,
              date,
              new DeviceInfo("osType", "name", "version", "model"),
              None
            )
            //println(purchaseInfo)
            purchaseService.save(companyName, applicationName, purchaseInfo)
          } else
            Future.successful()
        }**/
      })
    }
    Future.sequence(result) map {a => true}// map {a => println("Setup done!")}

    //Future {}
  }

  def execute(companyName: String, applicationName: String) = Action.async {
    val user = new User("userName", "me@mail.com", "1", companyName, List[String]())
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

    for{
      u <- userService.insertUser(user)
      a <- applicationService.insertApplication(companyName, application)
      x <- userService.addApplication(user.name, applicationName)
    } yield {
      Ok
    }
  }
}

  * */
