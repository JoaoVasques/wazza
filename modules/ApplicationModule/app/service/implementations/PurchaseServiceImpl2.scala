package service.application.implementations

import play.api.libs.json._
import play.api.libs.functional.syntax._
import service.application.definitions.{PurchaseService2}
import models.application.{PurchaseInfo2}
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService

class PurchaseServiceImpl2 @Inject()(
  databaseService: DatabaseService
) extends PurchaseService2 {

  private val PurchaseId = "id"

  databaseService.init(PurchaseInfo2.PurchaseCollection)

  def save(info: PurchaseInfo2): Try[Unit] = {
    databaseService.insert(Json.toJson(info))
  }

  def get(id: String): Option[PurchaseInfo2] = {
    databaseService.get(PurchaseId, id) match {
      case Some(purchase) => {
        purchase.validate[PurchaseInfo2].fold(
          valid = (p => Some(p)),
          invalid = (_ => None)
        )
      }
      case None => None
    }
  }

  def exist(id: String): Boolean = {
    this.get(id) match {
      case Some(_) => true
      case None => false
    }
  }

  def delete(info: PurchaseInfo2): Try[Unit] = {
    databaseService.delete(Json.toJson(info))
  }
}

