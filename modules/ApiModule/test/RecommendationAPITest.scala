package test.api

import models.application.Credentials
import models.application.Item
import models.application.VirtualCurrency
import models.application.WazzaApplication
import models.user.DeviceInfo
import models.user.MobileSession
import models.user.PurchaseInfo
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Controller
import scala.util.Failure
import scala.util.Success
import com.google.inject._
import service.persistence.implementations.MongoDatabaseService
import service.application.implementations.ApplicationServiceImpl
import service.application.implementations.ItemServiceImpl
import service.user.implementations.PurchaseServiceImpl
import service.user.implementations.MobileUserServiceImpl
import service.aws.implementations.PhotosServiceImpl
import service.security.implementations.SecretGeneratorServiceImpl
import service.security.definitions.SecretGeneratorServiceContext._
import models.application.InAppPurchaseContext._
import scala.concurrent._
import ExecutionContext.Implicits.global

class RecommendationAPITest extends Specification {

  def appConfig() = {
    Map(
      "mongodb" -> Map(
        "dev" -> Map(
          "uri" -> "mongodb://localhost:27017/wazza-test"
        )
      )
    )
  }

  private var app: WazzaApplication = null
  private var databaseService: MongoDatabaseService = null
  private var applicationService: ApplicationServiceImpl = null
  private var mobileUserService: MobileUserServiceImpl = null
  private var purchaseService: PurchaseServiceImpl = null

  private val CompanyName = "CompanyTest"
  private val AppName = "RecTestApp"
  private val NrItems = 100
  private val MaxPrice = 10
  private val NrMobileUsers = 1000
  private val NrPurchases = 500

  private def generateApp() = {
    val photosService = new PhotosServiceImpl
    val secretGeneratorService = new SecretGeneratorServiceImpl
    this.applicationService = new ApplicationServiceImpl(photosService, this.databaseService)
    val application = new WazzaApplication(
      AppName,
      "http://www.test.com",
      "image",
      "com.test",
      Some(WazzaApplication.applicationTypes.last), //Android
      new Credentials(
        secretGeneratorService.generateSecret(Id),
        secretGeneratorService.generateSecret(ApiKey),
        secretGeneratorService.generateSecret(ApiKey)
      ),
      List[Item](),
      List[VirtualCurrency]()
    )

    this.applicationService.insertApplication(CompanyName, application)
  }

  private def generateItems() = {
    var i = 0

    val itemService = new ItemServiceImpl(this.applicationService)
    for(i <- 1 to NrItems) {
      itemService.createGooglePlayItem(
        CompanyName,
        this.app.name,
        s"name-$i",
        s"description-$i",
        RealWordCurrencyType,
        None,
        i % MaxPrice,
        "published",
        "purchaseType",
        true,
        true,
        "Portuguese",
        "imageName",
        "imageUrl"
      ) map { item => {
        this.applicationService.addItem(CompanyName, item.get, this.app.name)
      }
      }
    }
  }

  private def generateMobileUsers() = {
    this.mobileUserService = new MobileUserServiceImpl(this.databaseService)
    var i = 0
    for(i <- 1 to NrMobileUsers) {
      this.mobileUserService.createMobileUser(
        CompanyName,
        this.app.name,
        i.toString,
        None,
        None
      )
    }
  }

  private def generatePurchases() = {
    this.purchaseService = new PurchaseServiceImpl(this.mobileUserService, this.databaseService)
    var i = 0
    for(i <- 1 to NrPurchases) {
      this.purchaseService.save(
        CompanyName,
        this.app.name,
        new PurchaseInfo(
          s"pId-$i",
          i.toString,
          this.app.name,
          s"name-$i",
          i % MaxPrice,
          "time",
          new DeviceInfo("osType", "name", "version", "model"),
          None),
        i.toString
      )
    }
  }

  private def init() = {
    
    try {
      this.databaseService = new MongoDatabaseService
    } catch {
      case e: Exception => {
        println(s"error $e")
      }
    }

    this.app = generateApp.get
    generateItems
    generateMobileUsers
    generatePurchases
  }

  "Recommendation API test" should {
    running(FakeApplication(additionalConfiguration = appConfig)) {
      init()
      "Test" in {
        true must equalTo(true)
      }
    }
  }
}

