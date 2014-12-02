package service.user.implementations

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import models.user.DeviceInfo
import models.user.LocationInfo
import models.user.MobileUser
import org.bson.types.ObjectId
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.Failure
import scala.util.Success
import service.user.definitions._
import models.user.{PurchaseInfo, Buyer}
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService
import java.util.Locale
import scala.concurrent._
import ExecutionContext.Implicits.global

class PurchaseServiceImpl @Inject()(
  userService: MobileUserService,
  databaseService: DatabaseService,
  mobileSessionService: MobileSessionService
) extends PurchaseService {

  def create(json: JsValue): PurchaseInfo = {
    new PurchaseInfo(
      (json \ "id").as[String],
      (json \ "sessionId").as[String],
      (json \ "userId").as[String],
      (json \ "itemId").as[String],
      (json \ "price").as[Double],
      (json \ "time").as[Date],
      (json \ "deviceInfo").as[DeviceInfo],
      (json \ "location").validate[LocationInfo] match {
        case success: JsSuccess[LocationInfo] => Some(success.value)
        case JsError(errors) => None
      }
    )
  }

  def save(companyName: String, applicationName: String, info: PurchaseInfo): Future[Unit] = {
    val collection = PurchaseInfo.getCollection(companyName, applicationName)
    exist(companyName, applicationName, info.id) flatMap {exists =>
      if(!exists) {
        databaseService.insert(collection, Json.toJson(info)) flatMap {r =>
          saveUserAsBuyer(companyName, applicationName, info.userId)
        }
      } else {
        Future.failed(new Exception("Duplicated purchase"))
      }
    }
  }

  def get(companyName: String, applicationName: String, id: String): Future[Option[PurchaseInfo]] = {
    val collection = PurchaseInfo.getCollection(companyName, applicationName)
    databaseService.get(collection, PurchaseInfo.Id, id) map { opt =>
      opt match {
        case Some(purchase) => {
          purchase.validate[PurchaseInfo].fold(
            valid = (p => Some(p)),
            invalid = (_ => None)
          )
        }
        case None => None
      }
    }
  }

  def getUserPurchases(companyName: String, applicationName: String, userId: String): Future[List[PurchaseInfo]] = {
    val collecion = PurchaseInfo.getCollection(companyName, applicationName)
    databaseService.getListElements(collecion, PurchaseInfo.UserId, userId) map (purchase => {
      purchase map {pm =>
        pm.validate[PurchaseInfo].fold(
          valid = (p => p),
          invalid = (_ => null)
        )
      }
    })
  }

  def exist(companyName: String, applicationName: String, id: String): Future[Boolean] = {
    this.get(companyName, applicationName, id) map {opt =>
      opt match {
        case Some(_) => true
        case None => false
      }
    }
  }

  def delete(companyName: String, applicationName: String, info: PurchaseInfo): Future[Unit] = {
    exist(companyName, applicationName, info.id.toString) flatMap {exists =>
      if(exists) {
        val collection = PurchaseInfo.getCollection(companyName, applicationName)
        databaseService.delete(collection, Json.toJson(info))
      } else {
        Future {new Exception("Cannot delete purchase that does not exist")}
      }
    }
  }

  private def saveUserAsBuyer(companyName: String, applicationName: String, userId: String) = {
    val collection = Buyer.getCollection(companyName, applicationName)
    databaseService.exists(collection, Buyer.Id, userId) flatMap {res =>
      if(!res) {
        databaseService.insert(collection, Json.obj("userId" -> userId))
      } else {
        Future{}
      }
    }
  }
}

