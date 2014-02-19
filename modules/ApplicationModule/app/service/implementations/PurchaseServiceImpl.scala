package service.application.implementations

import com.mongodb.casbah.commons.MongoDBObject
import models.application.PurchaseInfo
import service.application.definitions.{PurchaseService}
import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import scala.util.{Try, Success, Failure}

class PurchaseServiceImpl extends PurchaseService {

  private val dao = PurchaseInfo.getDAO

  def save(info: PurchaseInfo): Try[Unit] = {
    if(exist(info.itemId)) {
      Failure(new Exception("Purchase already exists"))
    } else {
     dao.insert(info)
     new Success()
    }
  }

  def get(id: String): Option[PurchaseInfo] = {
    dao.findOne(MongoDBObject("_id" -> id))
  }

  def exist(id: String): Boolean = {
    get(id) match {
      case Some(_) => true
      case None => false
    }
  }
}

