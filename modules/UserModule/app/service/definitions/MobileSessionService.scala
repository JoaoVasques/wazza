package service.user.definitions

import models.user.MobileSession
import play.api.libs.json.JsValue
import scala.util.Try

trait MobileSessionService {

  def create(json: JsValue): Try[MobileSession]

  def insert(companyName: String, applicationName: String, session: MobileSession): Try[Unit]

  def exists(id: String): Boolean

  def update(id: String, date: String, purchaseId: String): Try[Unit]

}

