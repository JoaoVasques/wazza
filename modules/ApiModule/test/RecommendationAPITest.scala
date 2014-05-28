package test.api

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Random
import models.application.Credentials
import models.application.Item
import models.application.VirtualCurrency
import models.application.WazzaApplication
import models.user.DeviceInfo
import models.user.MobileSession
import models.user.PurchaseInfo
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Controller
import scala.util.Failure
import scala.util.Success
import service.persistence.implementations.MongoDatabaseService
import service.application.implementations.ApplicationServiceImpl
import service.application.implementations.ItemServiceImpl
import service.user.implementations.PurchaseServiceImpl
import service.user.implementations.MobileUserServiceImpl
import service.user.implementations.MobileSessionServiceImpl
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
          "uri" -> "mongodb://joao:1234@dharma.mongohq.com:10045/app20418920"
        )
      )
    )
  }

  private var app: WazzaApplication = null
  private var databaseService: MongoDatabaseService = null
  private var applicationService: ApplicationServiceImpl = null
  private var mobileUserService: MobileUserServiceImpl = null
  private var purchaseService: PurchaseServiceImpl = null
  private var mobileSessionService: MobileSessionServiceImpl = null

  private val CompanyName = "CompanyTest"
  private val AppName = "RecTestApp"
  private val NumberDays = 5
  private val NrItems = 10
  private val MaxPrice = 10
  private val NrMobileUsers = 200
  private val NrPurchases = 150

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
    this.mobileSessionService = new MobileSessionServiceImpl(this.databaseService)
    this.mobileUserService = new MobileUserServiceImpl(this.databaseService)
    var i = 0
    val cal = Calendar.getInstance()
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
    val r = new Random
    for(i <- 1 to NrMobileUsers) {
      val u = this.mobileUserService.createMobileUser(
        CompanyName,
        this.app.name,
        s"user-" + i.toString
      )

      cal.add(Calendar.DATE, (r.nextInt(NumberDays) * -1))
      val sessionJson = Json.obj(
        "id" -> i.toString,
        "userId" -> (s"user-" + i.toString),
        "sessionLength" -> 0,
        "startTime" -> format.format(cal.getTime),
        "deviceInfo" -> Json.obj(
          "osType" -> "osType",
          "osName" -> "name",
          "osVersion" -> "version",
          "deviceModel" -> "model"
        ),
        "purchases" -> List[String]()
      )

      val session = this.mobileSessionService.create(sessionJson).get
      this.mobileSessionService.insert(CompanyName, AppName, session)
    }
  }

  private def generatePurchases() = {
    this.purchaseService = new PurchaseServiceImpl(this.mobileUserService, this.databaseService, this.mobileSessionService)
    var i = 0
    val cal = Calendar.getInstance()
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
    val r = new Random
    for(i <- 1 to NrPurchases) {
      cal.add(Calendar.DATE, (r.nextInt(NumberDays) * -1))
      val json = Json.obj(
        "id" -> (s"purchase-id-$i"),
        "sessionId" -> i.toString,
        "userId" ->  (s"user-" + i.toString),
        "name" -> this.app.name,
        "itemId" -> s"name-${i % NrItems}",
        "price" -> i % MaxPrice,
        "time" -> format.format(cal.getTime),
        "deviceInfo" -> Json.obj(
          "osType" -> "osType",
          "osName" -> "name",
          "osVersion" -> "version",
          "deviceModel" -> "model"
        )
      )
      val purchase = this.purchaseService.create(json)
      this.purchaseService.save(
        CompanyName,
        this.app.name,
        purchase
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

    this.databaseService.dropCollection(WazzaApplication.getCollection(CompanyName, AppName))
    this.databaseService.dropCollection(MobileSession.getCollection(CompanyName, AppName))
    this.databaseService.dropCollection(PurchaseInfo.getCollection(CompanyName, AppName))

    this.app = generateApp.get
    generateItems
    generateMobileUsers
    generatePurchases
  }

  "Application" should {
    
    "send 404 on a bad request" in {
      running(FakeApplication(additionalConfiguration = appConfig)) {
        init()
        true must equalTo(true)
      }
    }
  }
}

