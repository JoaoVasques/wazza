package service.application.definitions

import models.application.{PurchaseInfo2}
import scala.util.Try

trait PurchaseService2 {

  def save(info: PurchaseInfo2): Try[Unit]

  def get(id: String): Option[PurchaseInfo2]

  def exist(id: String): Boolean

  def delete(info: PurchaseInfo2): Try[Unit]
}

