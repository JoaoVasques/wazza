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

class GenerateSessionsController @Inject()(
  applicationService: ApplicationService,
  userService: UserService,
  purchaseService: PurchaseService,
  mobileSessionService: MobileSessionService
) extends Controller {

  private lazy val NumberMobileUsers = 70
  
  private def generateSessions(companyName: String, applicationName: String): Future[Boolean] = {
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
        val session = new MobileSession(
          (s"${currentDay.toString}-$userNumber"), //hash
          userNumber.toString,
          2,
          format.format(currentDay.toDate),
          new DeviceInfo("osType", "name", "version", "model"),
          List[String]() //List of purchases id's
        )
        mobileSessionService.insert(companyName, applicationName, session)
      }) map {a => a.head}
    }
    Future.sequence(result) map {a => println(a) ; true}// map {a => println("Setup done!")}
  }

  def execute(companyName: String, applicationName: String) = Action.async {
    generateSessions(companyName, applicationName) map {r =>
      Ok
    }
  }
}

