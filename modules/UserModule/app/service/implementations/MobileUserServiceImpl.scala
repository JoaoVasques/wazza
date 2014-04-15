package service.user.implementations

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

class MobileUserServiceImpl @Inject()(
  databaseService: DatabaseService
) extends MobileUserService {

  private val UserId = "userId"
  private val SessionId = "sessions"

  def updateMobileUserSession(
    companyName: String,
    applicationName: String,
    userId: String,
    session: MobileSession
  ): Try[Unit] = {

    val collection = MobileUser.getCollection(companyName, applicationName)

    if(mobileUserExists(companyName, applicationName, userId)) {
      databaseService.addElementToArray[JsValue](
        collection,
        UserId,
        userId,
        SessionId,
        Json.toJson(session)
      )
    } else {
      val user = new MobileUser(
        userId,
        session.deviceInfo.osType,
        List[MobileSession](session),
        List[PurchaseInfo]()
      )
      databaseService.insert(collection, Json.toJson(user))
    }
  }

  def createMobileUser(
    companyName: String,
    applicationName: String,
    userId: String,
    sessions: Option[List[MobileSession]],
    purchases: Option[List[PurchaseInfo]]
  ): Try[Unit] = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    if(!mobileUserExists(companyName, applicationName, userId)) {
      val osType = sessions match {
        case Some(s) => s.head.deviceInfo.osType
        case None => {
          purchases match {
            case Some(p) => null
            case None => "unknown"
          }
        }
      }

      val user = new MobileUser(
        userId,
        osType,
        sessions match {
          case Some(s) => s
          case None => List[MobileSession]()
        },
        purchases match {
          case Some(p) => p
          case None => List[PurchaseInfo]()
        }
      )
      databaseService.insert(collection, Json.toJson(user))
    } else {
      new Failure(new Exception("Duplicated mobile user"))
    }
  }

  def createSession(json: JsValue): Try[MobileSession] = {
    (json).validate[MobileSession] match {
      case session: JsSuccess[MobileSession] => {
        Success(session.get)
      }
      case e: JsError => new Failure(new Exception(JsError.toFlatJson(e).toString))
    }
  }

  def mobileUserExists(companyName: String, applicationName: String, userId: String): Boolean = {
    val collection = MobileUser.getCollection(companyName, applicationName)
    databaseService.exists(collection, UserId, userId)
  }
}

