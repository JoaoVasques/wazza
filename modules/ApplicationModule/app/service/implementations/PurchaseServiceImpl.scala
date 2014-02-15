package service.application.implementations

import play.api.libs.json._
import play.api.libs.functional.syntax._
import service.application.definitions.{PurchaseService}
import models.application.{PurchaseInfo}
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService

class PurchaseServiceImpl @Inject()(
  databaseService: DatabaseService
) extends PurchaseService {

  private val PurchaseId = "id"

  databaseService.init(PurchaseInfo.PurchaseCollection)

  def save(info: PurchaseInfo): Try[Unit] = {
    databaseService.insert(Json.toJson(info))
  }

  def get(id: String): Option[PurchaseInfo] = {
    databaseService.get(PurchaseId, id) match {
      case Some(purchase) => {
        purchase.validate[PurchaseInfo].fold(
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

  def delete(info: PurchaseInfo): Try[Unit] = {
    databaseService.delete(Json.toJson(info))
  }
}

