package test.application

import org.specs2.mutable._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import com.google.inject._
import scala.util.Failure
import models.application._
import service.application.implementations.ApplicationServiceImpl
import service.aws.implementations.PhotosServiceImpl
import service.persistence.implementations.MongoDatabaseService

class DemoBootstrapTest  extends Specification {
  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size(11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }
}

private[application] object Setup {

  private object ApplicationData {
    val name = "Demo"
    val appUrl = "www.example.com"
    val imageName = "image-test"
    val packageName = "com.example"
    val appType = WazzaApplication.applicationTypes.last
    val credentials = new Credentials("id", "key", "sdk")
    val items = List[Item]()
    val virtualCurrencies = List[VirtualCurrency]()
  }

  def execute() = {

  }

  private def createApplication = {

  }


}

/**
import models.application.{Credentials, Currency, GoogleMetadata, GoogleTranslations, ImageInfo, InAppPurchaseMetadata, Item, VirtualCurrency, WazzaApplication}
import org.specs2.mutable._
import play.api.test.FakeApplication
import play.api.test.Helpers._
import scala.util.Failure
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
    val translate = new GoogleTranslations("locale", "title", "description")
    val metadata = new GoogleMetadata(
      InAppPurchaseMetadata.Android,
      name,
      "title",
      description,
      "published",
      "managed_by_publisher",
      false,
      List[GoogleTranslations](translate),
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
    mongoDBService.dropCollection()
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
      }

      "Get Item" in {
        applicationService.getItem(item.name, application.name) must equalTo(Some(item))
        applicationService.itemExists(item.name, application.name) must equalTo(true)
      }
    }
  }
}
  * */
