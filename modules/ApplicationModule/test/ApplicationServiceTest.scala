package test.application

import models.application.Currency
import models.application.GoogleMetadata
import models.application.GoogleTranslations
import models.application.ImageInfo
import models.application.InAppPurchaseMetadata
import models.application.Item
import models.application.VirtualCurrency
import models.application.{WazzaApplication, Credentials}
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import scala.util.Success
import service.application.implementations.ApplicationServiceImpl
import service.aws.implementations.PhotosServiceImpl
import service.persistence.implementations.MongoDatabaseService

class ApplicationServiceTest extends Specification {

  private object ApplicationData {
    val name = "app name"
    val appUrl = "www.example.com"
    val imageName = "image-test"
    val packageName = "com.example"
    val appType = WazzaApplication.applicationTypes.last
    val credentials = new Credentials("id", "key", "sdk")
    val items = List[Item]()
    val virtualCurrencies = List[VirtualCurrency]()
  }

  private object ItemData {
    val name = "item"
    val description = "item description"
    val store = 0
    val metadata = new GoogleMetadata(
      InAppPurchaseMetadata.Android,
      name,
      "title",
      description,
      "published",
      "managed_by_publisher",
      false,
      List[GoogleTranslations](),
      false,
      "PT",
      1.99,
      List("PT")
    )
    val currency = new Currency(1, 1.99, None)
    val imageInfo = new ImageInfo("name", "http://www.example.com")
  }

  private def initApplicationService(): ApplicationServiceImpl = {
    val uri = "mongodb://localhost:27017/wazza-test"
    val photosService = new PhotosServiceImpl
    val mongoDBService = new MongoDatabaseService
    mongoDBService.init(uri, "applicationsTest")
    new ApplicationServiceImpl(photosService, mongoDBService)
  }

  "Application Operations" should {
    running(FakeApplication()){
      val application = new WazzaApplication(
        ApplicationData.name,
        ApplicationData.appUrl,
        ApplicationData.imageName,
        ApplicationData.packageName,
        Some(ApplicationData.appType),
        ApplicationData.credentials,
        ApplicationData.items,
        ApplicationData.virtualCurrencies
      )

      val item = new Item(
        ItemData.name,
        ItemData.description,
        ItemData.store,
        ItemData.metadata,
        ItemData.currency,
        ItemData.imageInfo
      )

      val applicationService = initApplicationService
      "Insert" in {
        applicationService.insertApplication(application) must equalTo(Success(application))
        applicationService.find(application.name).get must equalTo(application)
      }

      "Delete" in {
        applicationService.deleteApplication(application) must equalTo(Success())
        applicationService.find(application.name) must equalTo(None)
      }

      "Insert item" in {
        // re-add application        
        applicationService.insertApplication(application) must equalTo(Success(application))
        applicationService.addItem(item, application.name) must equalTo(Success(item))
        //applicationService.getItem(item.name, application.name) must equalTo(Some(item))
        //applicationService.itemExists(item.name, application.name) must equalTo(true)
      }
    }
  }
}

