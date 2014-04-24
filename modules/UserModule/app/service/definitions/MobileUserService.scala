package service.user.definitions

import models.user.{MobileUser}
import models.user.{MobileSession}
import models.user.{PurchaseInfo}
import play.api.libs.json.JsValue
import scala.concurrent.Future
import scala.util.Try

trait MobileUserService {

  def updateMobileUserSession(
    companyName: String,
    applicationName: String,
    userId: String,
    session: MobileSession
  ): Try[Unit]

  def createMobileUser(
    companyName: String,
    applicationName: String,
    userId: String,
    sessions: Option[List[MobileSession]]
  ): Try[MobileUser]

  def get(companyName: String, applicationName: String, userId: String): Option[MobileUser]

  def mobileUserExists(companyName: String, applicationName: String, userId: String): Boolean
}
