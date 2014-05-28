package service.user.definitions

import models.user.{PurchaseInfo}
import play.api.libs.json.JsValue
import scala.util.Try

trait PurchaseService {

  def create(json: JsValue): PurchaseInfo

  def save(companyName: String, applicationName: String, info: PurchaseInfo): Try[Unit]

  def get(companyName: String, applicationName: String, id: String): Option[PurchaseInfo]

  def getUserPurchases(companyName: String, applicationName: String, userId: String): List[PurchaseInfo]

  def exist(companyName: String, applicationName: String, id: String): Boolean

  def delete(companyName: String, applicationName: String, info: PurchaseInfo): Try[Unit]
}

