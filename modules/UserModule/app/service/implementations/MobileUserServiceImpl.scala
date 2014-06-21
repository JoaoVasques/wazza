package service.user.implementations

import org.bson.types.ObjectId
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.user.definitions.MobileUserService
import models.user.{MobileUser}
import models.user.{MobileSession}
import models.user.{DeviceInfo}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.implicitConversions
import com.google.inject._
import service.persistence.definitions.{DatabaseService}
import play.api.libs.json.Json
import models.user.PurchaseInfo
import java.util.Date

class MobileUserServiceImpl @Inject()(
  databaseService: DatabaseService
) extends MobileUserService {
  
  def createMobileUser(
    companyName: String,
    applicationName: String,
    userId: String
  ): Try[MobileUser] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    if(!exists(companyName, applicationName, userId)) {
      val user = new MobileUser(userId)
      databaseService.insert(collection, user) match {
        case Success(_) => new Success(user)
        case Failure(f) => new Failure(f)
      }
    } else {
      new Failure(new Exception("Duplicated mobile user"))
    }
  }

  def get(companyName: String, applicationName: String, userId: String): Option[MobileUser] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    databaseService.get(
      collection,
      MobileUser.KeyId,
      userId
    ) match {
      case Some(j) => Some(j)
      case None => None
    }
  }

  def exists(companyName: String, applicationName: String, userId: String): Boolean = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    databaseService.exists(collection, MobileUser.KeyId, userId)
  }
}

