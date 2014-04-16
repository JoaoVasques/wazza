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

  databaseService.init(MobileUser.MobileUserCollection)

  def save(info: PurchaseInfo, userId: String): Try[Unit] = {
    if(!userService.mobileUserExists(userId)) {
      userService.createMobileUser(
        userId,
        None,
        Some(List[PurchaseInfo](info))
      ) match {
        case Success(u) => {
          databaseService.addElementToArray[JsValue](
            UserId,
            userId,
            PurchaseId,
            Json.toJson(info)
          )
        }
        case Failure(f) => Failure(f)
      }
    } else {
      if(exist(info.id, userId)) {
        new Failure(new Exception("Duplicated purchase"))
      } else {
        databaseService.addElementToArray[JsValue](
          UserId,
          userId,
          PurchaseId,
          Json.toJson(info)
        )
      }
    }
  }

  def get(id: String, userId: String): Option[PurchaseInfo] = {
    databaseService.getElementFromArray[String](
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

  def exist(id: String, userId: String): Boolean = {
    this.get(id, userId) match {
      case Some(_) => true
      case None => false
    }
  }

  def delete(info: PurchaseInfo, userId: String): Try[Unit] = {
    if(exist(info.id, userId)) {
      databaseService.deleteElementFromArray[String](
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

