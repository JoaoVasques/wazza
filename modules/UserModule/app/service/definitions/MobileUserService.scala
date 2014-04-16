package service.user.definitions

import models.user.{MobileUser}
import models.user.{MobileSession}
import play.api.libs.json.JsValue
import scala.concurrent.Future
import scala.util.Try

trait MobileUserService {

  def updateMobileUserSession(userId: String, session: MobileSession): Try[Unit]

  def createSession(json: JsValue): Try[MobileSession]

  def mobileUserExists(userId: String): Boolean
}
