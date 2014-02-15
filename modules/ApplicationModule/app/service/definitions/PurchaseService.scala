package service.application.definitions

import models.application.{PurchaseInfo}
import scala.util.Try

trait PurchaseService {

  def save(info: PurchaseInfo): Try[Unit]

  def get(id: String): Option[PurchaseInfo]

  def exist(id: String): Boolean

  def delete(info: PurchaseInfo): Try[Unit]
}

