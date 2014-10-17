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
import scala.concurrent.duration._
import scala.util.Random
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class GeneratePurchasesController @Inject()(
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

  private def generatePurchases(companyName: String, applicationName: String): Future[Unit] = {
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
      Future.sequence((1 to NumberMobileUsers) map {userNumber =>
        val makePurchases = willMakePurchases
        if(makePurchases) {
          val itemsAux = items
          val item = Random.shuffle(itemsAux).head
          val date = format.format(currentDay.toDate)
          val purchaseInfo = new PurchaseInfo(
            s"purchase-$userNumber-${currentDay.toString}",
            (s"${currentDay.toString}-$userNumber"),
            userNumber.toString,
            item._1,
            item._2,
            date,
            new DeviceInfo("osType", "name", "version", "model"),
            None
          )
          purchaseService.save(companyName, applicationName, purchaseInfo)
        } else
          Future.successful()
      })
    }

    Future.sequence(result) map {a => println}
  }

  def execute(companyName: String, applicationName: String) = Action.async {
    generatePurchases(companyName, applicationName) map {a =>
      Ok
    }
  }
}

