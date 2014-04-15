package service.user.definitions

import models.user.{PurchaseInfo}
import scala.util.Try

trait PurchaseService {

  def save(companyName: String, applicationName: String, info: PurchaseInfo, userId: String): Try[Unit]

  def get(companyName: String, applicationName: String, id: String, userId: String): Option[PurchaseInfo]

  def exist(companyName: String, applicationName: String, id: String, userId: String): Boolean

  def delete(companyName: String, applicationName: String, info: PurchaseInfo, userId: String): Try[Unit]
}

