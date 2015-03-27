package service.user.definitions

import models.user.{MobileUser}
import models.user.{MobileSession}
import models.payments.{PurchaseInfo}
import play.api.libs.json.JsValue
import scala.concurrent.Future
import scala.util.Try

trait MobileUserService {

  def createMobileUser(
    companyName: String,
    applicationName: String,
    userId: String
  ): Future[Unit]

  def get(companyName: String, applicationName: String, userId: String): Future[Option[MobileUser]]

  def exists(companyName: String, applicationName: String, userId: String): Future[Boolean]
}
