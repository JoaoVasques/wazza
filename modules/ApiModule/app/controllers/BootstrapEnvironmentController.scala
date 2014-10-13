package controllers.api

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

class BootstrapEnvironmentController @Inject()(
  applicationService: ApplicationService,
  userService: UserService,
  purchaseController: PurchaseController
) extends Controller {

  private lazy val LowerPrice = 1.99
  private lazy val UpperPrice = 5.99

  /**
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

    * */

  private object ApplicationData {
    val appUrl = "www.example.com"
    val imageName = "image-test"
    val packageName = "com.example"
    val appType = WazzaApplication.applicationTypes.last
    val credentials = new Credentials("id", "key", "sdk")
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

      val items = generateItems(10)
      val start = new LocalDate()
      val end = start.minusDays(7)
      //val dates = new ListBuffer[String]()
      val days = Days.daysBetween(start, end).getDays()+1

      lazy val NumberMobileUsers = 700
      lazy val NumberPurchases = 70

      List.range(0, days) foreach {index =>
        val currentDay = start.withFieldAdded(DurationFieldType.days(), index)

      }

      applicationService.find(companyName, applicationName) map { println(_)}

    }

    Future.successful(Ok("todo"))
  }
}

