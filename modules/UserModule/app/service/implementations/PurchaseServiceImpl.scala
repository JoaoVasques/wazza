package service.user.implementations

import models.user.MobileUser
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

class PurchaseServiceImpl @Inject()(
  userService: MobileUserService,
  databaseService: DatabaseService
) extends PurchaseService {

  private val UserId = "userId"
  private val PurchaseId = "purchases"


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
        }
        case Failure(f) => Failure(f)
      }
    } else {
      if(exist(companyName, applicationName, info.id, userId)) {
        new Failure(new Exception("Duplicated purchase"))
      } else {
        databaseService.addElementToArray[JsValue](
          collection,
          UserId,
          userId,
          PurchaseId,
          Json.toJson(info)
        )
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
    if(exist(companyName, applicationName, info.id, userId)) {
      val collection = PurchaseInfo.getCollection(companyName, applicationName)
      databaseService.deleteElementFromArray[String](
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

