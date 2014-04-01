package service.user.definitions

import models.user.{PurchaseInfo}
import scala.util.Try

trait PurchaseService {

  def save(info: PurchaseInfo, userId: String): Try[Unit]

  def get(id: String, userId: String): Option[PurchaseInfo]

  def exist(id: String, userId: String): Boolean

  def delete(info: PurchaseInfo, userId: String): Try[Unit]
}

