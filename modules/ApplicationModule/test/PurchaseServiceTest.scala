package test.application

import models.application.{LocationInfo, PurchaseInfo}
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Controller
import scala.util.Failure
import scala.util.Success
import com.google.inject._
import service.application.implementations.{PurchaseServiceImpl}
import service.persistence.implementations.{MongoDatabaseService}

class PurchaseServiceTest extends Specification {

  private object PurchaseData {
    val id = "id"
    val applicationName = "app name"
    val itemId = "itemId"
    val price = 1.99
    val time = "today"
    val location = new LocationInfo(1,2)
  }

  private def initPurchaseService(): PurchaseServiceImpl = {
    val uri = "mongodb://localhost:27017/wazza-test"
    val mongoDbService = new MongoDatabaseService
    mongoDbService.init(uri, PurchaseInfo.PurchaseCollection)
    new PurchaseServiceImpl(mongoDbService)
  }

  "Purchase Operations" should {
    running(FakeApplication()) {
      val purchaseService = initPurchaseService
      val purchase = new PurchaseInfo(
        PurchaseData.id,
        PurchaseData.applicationName,
        PurchaseData.itemId,
        PurchaseData.price,
        PurchaseData.time,
        Some(PurchaseData.location)
      )

      "Insert" in {
        purchaseService.save(purchase) must equalTo(Success())
        purchaseService.exist(PurchaseData.id) must equalTo(true)
        purchaseService.get(PurchaseData.id).get must equalTo(purchase)
      }

      "Delete" in {
        purchaseService.delete(purchase) must equalTo(Success())
        purchaseService.get(PurchaseData.id) must equalTo(None)
      }
    }
  }
}

