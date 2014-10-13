package controllers.api

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import models.application._
import service.application.definitions._
import service.user.definitions._
import com.google.inject._
import scala.math.BigDecimal

class BootstrapEnvironmentController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller {

  private lazy val LowerPrice = 1.99
  private lazy val UpperPrice = 5.99

  private object ApplicationData {
    val appUrl = "www.example.com"
    val imageName = "image-test"
    val packageName = "com.example"
    val appType = WazzaApplication.applicationTypes.last
    val credentials = new Credentials("id", "key", "sdk")
    val items = List[Item]()
    val virtualCurrencies = List[VirtualCurrency]()
  }

  private class ItemData(name: String, price: Double) {
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
      price,
      List("PT")
    )
    val currency = new Currency(1, price, None)
    val imageInfo = new ImageInfo("name", "http://www.example.com")
  }

  private def generateItems(numberItems: Int): List[Item] = {

    def generateItemPrice(lowerPrice: Double, upperPrice: Double): Double = {
      lazy val DecimalPlaces = 2
      val price = (Math.random() * (upperPrice - lowerPrice)) + lowerPrice
      BigDecimal(price).setScale(DecimalPlaces, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    (1 to numberItems).map {i=>
      val itemData = new ItemData(s"name-$i", generateItemPrice(LowerPrice, UpperPrice))
      new Item(
        s"name-$i",
        itemData.description,
        itemData.store,
        itemData.metadata,
        itemData.currency,
        itemData.imageInfo
      )
    }.toList
  }

  def execute(companyName: String, applicationName: String) = Action.async {

    val application = new WazzaApplication(
      companyName,
      ApplicationData.appUrl,
      ApplicationData.imageName,
      ApplicationData.packageName,
      List(ApplicationData.appType),
      ApplicationData.credentials,
      ApplicationData.items,
      ApplicationData.virtualCurrencies
    )

    applicationService.insertApplication(companyName, application) flatMap {res =>
      println(s"Application $application.name created")
      Future.sequence(
        generateItems(10) map {item =>
          applicationService.addItem(companyName, item, applicationName)
        }
      )
    }

    println(generateItems(1))

    Future.successful(Ok("todo"))
  }
}

