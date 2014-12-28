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
import scala.language.implicitConversions
import com.google.inject._
import service.persistence.definitions.{DatabaseService}
import play.api.libs.json.Json
import models.user.PurchaseInfo
import java.util.Date
import scala.concurrent._
import ExecutionContext.Implicits.global

class MobileUserServiceImpl @Inject()(
  databaseService: DatabaseService
) extends MobileUserService {

  def createMobileUser(
    companyName: String,
    applicationName: String,
    userId: String
  ): Future[Unit] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    exists(companyName, applicationName, userId) flatMap {userExists =>
      if(!userExists) {
        val user = new MobileUser(userId)
        databaseService.insert(collection, user) map {res => res}
      } else {
        Future {new Exception("Duplicated mobile user")}
      }
    }
  }

  def get(companyName: String, applicationName: String, userId: String): Future[Option[MobileUser]] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    databaseService.get(collection, MobileUser.KeyId, userId) map {opt =>
      opt match {
        case Some(j) => Some(j)
        case None => None
      }
    }
  }

  def exists(companyName: String, applicationName: String, userId: String): Future[Boolean] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    databaseService.exists(collection, MobileUser.KeyId, userId)
  }
}

