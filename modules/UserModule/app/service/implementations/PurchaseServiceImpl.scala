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
import service.user.definitions.MobileUserService
import service.user.definitions.{PurchaseService}
import models.user.{PurchaseInfo}
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService
import utils.persistence._
import java.util.Locale

class PurchaseServiceImpl @Inject()(
  userService: MobileUserService,
  databaseService: DatabaseService
) extends PurchaseService {

  private val UserId = "userId"
  private val PurchaseId = "purchases"

  private def addPurchaseToRecommendationCollection(
    companyName: String,
    applicationName: String,
    purchaseId: Long,
    userId: Long
  ): Try[Unit] = {

    val date = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss 'GMT'Z (zzz)", Locale.ENGLISH).format(new Date())

    val collection = PurchaseInfo.getRecommendationCollection(companyName, applicationName)
    val info = Json.obj(
      "created_at" -> date
    )

    val objs = Map(
      "user_id" -> userId,
      "purchase_id" -> purchaseId
    )

    databaseService.insert(collection,info, objs)
  }

  def create(json: JsValue): PurchaseInfo = {
    val _id = new ObjectId
    new PurchaseInfo(
      PersistenceUtils.idToLong(new ObjectId),
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

  def save(companyName: String, applicationName: String, info: PurchaseInfo, userId: String): Try[Unit] = {

    val collection = PurchaseInfo.getCollection(companyName, applicationName)

    if(!userService.mobileUserExists(companyName, applicationName, userId)) {
      userService.createMobileUser(
        companyName,
        applicationName,
        userId,
        None,
        Some(List[PurchaseInfo](info))
      ) match {
        case Success(u) => {
          databaseService.addElementToArray[JsValue](
            collection,
            UserId,
            userId,
            PurchaseId,
            Json.toJson(info)
          )

          addPurchaseToRecommendationCollection(companyName, applicationName, info.id, u.dbId)
        }
        case Failure(f) => Failure(f)
      }
    } else {
      if(exist(companyName, applicationName, info.id.toString, userId)) {
        new Failure(new Exception("Duplicated purchase"))
      } else {
        databaseService.addElementToArray[JsValue](
          collection,
          UserId,
          userId,
          PurchaseId,
          Json.toJson(info)
        )

        val user = userService.get(companyName, applicationName, userId)

        addPurchaseToRecommendationCollection(
          companyName,
          applicationName,
          info.id,
          user.get.dbId)
      }
    }
  }

  def get(companyName: String, applicationName: String, id: String, userId: String): Option[PurchaseInfo] = {
    val collection = PurchaseInfo.getCollection(companyName, applicationName)
    databaseService.getElementFromArray[String](
      collection,
      UserId,
      userId,
      PurchaseId,
      "id",
      id
    ) match {
      case Some(purchase) => {
        purchase.validate[PurchaseInfo].fold(
          valid = (p => Some(p)),
          invalid = (_ => None)
        )
      }
      case None => None
    }
  }

  def exist(companyName: String, applicationName: String, id: String, userId: String): Boolean = {
    this.get(companyName, applicationName, id, userId) match {
      case Some(_) => true
      case None => false
    }
  }

  def delete(companyName: String, applicationName: String, info: PurchaseInfo, userId: String): Try[Unit] = {
    if(exist(companyName, applicationName, info.id.toString, userId)) {
      val collection = PurchaseInfo.getCollection(companyName, applicationName)
      databaseService.deleteElementFromArray[Long](
        collection,
        UserId,
        userId,
        PurchaseId,
        "id",
        info.id
      )
    } else {
      new Failure(new Exception("Cannot delete purchase that does not exist"))
    }
  }
}

