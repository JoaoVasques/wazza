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
import service.user.definitions.MobileSessionService
import service.user.definitions.MobileUserService
import service.user.definitions.{PurchaseService}
import models.user.{PurchaseInfo}
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService
import java.util.Locale

class PurchaseServiceImpl @Inject()(
  userService: MobileUserService,
  databaseService: DatabaseService,
  mobileSessionService: MobileSessionService
) extends PurchaseService {

  private def addPurchaseToRecommendationCollection(
    companyName: String,
    applicationName: String,
    purchaseId: String,
    userId: String
  ): Try[Unit] = {

    val date = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss 'GMT'Z (zzz)", Locale.ENGLISH).format(new Date())

    val collection = PurchaseInfo.getRecommendationCollection(companyName, applicationName)
    val info = Json.obj(
      "created_at" -> date
    )

    val pId = databaseService.get(
      PurchaseInfo.getCollection(companyName, applicationName),
      PurchaseInfo.Id,
      purchaseId,
      "_id"
    ).get
    
    val uID = databaseService.get(
      MobileUser.getCollection(companyName, applicationName),
      MobileUser.KeyId,
      userId,
      "_id").get

    val mobileUserObjectId = new ObjectId((uID \ "_id" \ "$oid").as[String])
    val purchaseObjectId = new ObjectId((pId \ "_id" \ "$oid").as[String])

    val objs = Map(
      "user_id" -> mobileUserObjectId,
      "purchase_id" -> purchaseObjectId
    )

    new Success
    databaseService.insert(collection,info, objs)
  }

  def create(json: JsValue): PurchaseInfo = {
    new PurchaseInfo(
      (json \ "id").as[String],
      (json \ "sessionId").as[String],
      (json \ "userId").as[String],
      (json \ "name").as[String],
      (json \ "itemId").as[String],
      (json \ "price").as[Double],
      (json \ "time").as[String],
      (json \ "deviceInfo").as[DeviceInfo],
      (json \ "location").validate[LocationInfo] match {
        case success: JsSuccess[LocationInfo] => Some(success.value)
        case JsError(errors) => None
      }
    )
  }

  def save(companyName: String, applicationName: String, info: PurchaseInfo): Try[Unit] = {
    val collection = PurchaseInfo.getCollection(companyName, applicationName)

    if(!exist(companyName, applicationName, info.id)) {
      databaseService.insert(collection, Json.toJson(info))
      val session = mobileSessionService.get(info.sessionId)
      mobileSessionService.addPurchase(companyName, applicationName, session.get, info.id)
      addPurchaseToRecommendationCollection(companyName, applicationName, info.id, info.userId)
    } else {
      new Failure(new Exception("Duplicated purchase"))
    }
  }

  def get(companyName: String, applicationName: String, id: String): Option[PurchaseInfo] = {
    val collection = PurchaseInfo.getCollection(companyName, applicationName)
    databaseService.get(
      collection,
      PurchaseInfo.Id,
      id
    ) match {
      case Some(purchase) => {
        val purchaseMap = purchase.as[Map[String, JsValue]]
        val updated = purchaseMap + ("time" -> purchaseMap.get("time").map {t =>
          (t \ "$date").as[JsString]
        }.get)

        PurchaseInfo.buildJsonFromMap(updated).validate[PurchaseInfo].fold(
          valid = (p => Some(p)),
          invalid = (_ => None)
        )
      }
      case None => None
    }
  }

  def getUserPurchases(companyName: String, applicationName: String, userId: String): List[PurchaseInfo] = {
    val collecion = PurchaseInfo.getCollection(companyName, applicationName)
    databaseService.getListElements(collecion, PurchaseInfo.UserId, userId) map (purchase => {
      val purchaseMap = purchase.as[Map[String, JsValue]]
      val updated = purchaseMap + ("time" -> purchaseMap.get("time").map {t =>
        (t \ "$date").as[JsString]
      }.get)

      PurchaseInfo.buildJsonFromMap(updated).validate[PurchaseInfo].fold(
        valid = (p => p),
        invalid = (_ => null)
      )
    })
  }

  def exist(companyName: String, applicationName: String, id: String): Boolean = {
    this.get(companyName, applicationName, id) match {
      case Some(_) => true
      case None => false
    }
  }

  def delete(companyName: String, applicationName: String, info: PurchaseInfo): Try[Unit] = {
    if(exist(companyName, applicationName, info.id.toString)) {
      val collection = PurchaseInfo.getCollection(companyName, applicationName)
      databaseService.delete(collection, Json.toJson(info))
    } else {
      new Failure(new Exception("Cannot delete purchase that does not exist"))
    }
  }
}

