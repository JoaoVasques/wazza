package service.user.definitions

import models.user.MobileSession
import play.api.libs.json.JsValue
import scala.util.Try

trait MobileSessionService {

  def create(json: JsValue): Try[MobileSession]

  def insert(companyName: String, applicationName: String, session: MobileSession): Try[Unit]

  def get(hash: String): Option[MobileSession]

  def exists(id: String): Boolean

  def addPurchase(session: MobileSession, purchaseId: String)

  def calculateSessionLength(session: MobileSession, dateStr: String)
}

