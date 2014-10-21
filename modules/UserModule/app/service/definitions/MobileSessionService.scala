package service.user.definitions

import models.user.MobileSession
import play.api.libs.json.JsValue
import scala.util.Try
import scala.concurrent._

trait MobileSessionService {

  def create(json: JsValue): Try[MobileSession]

  def insert(companyName: String, applicationName: String, session: MobileSession): Future[Unit]

  def get(hash: String): Future[Option[MobileSession]]

  def exists(id: String): Future[Boolean]

  def calculateSessionLength(session: MobileSession, dateStr: String): Future[Unit]
}

