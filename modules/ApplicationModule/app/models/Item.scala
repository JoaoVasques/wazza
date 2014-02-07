package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import ApplicationMongoContext._

case class Item(
  @Key("_id") name: String,
  description: String,
  store: Int,
  metadata: ItemMetadata,
  currency: Currency
)

case class Currency(
  typeOf: Int, //virtual or real money
  value: Double
)

@Salat
trait ItemMetadata {
  def osType: String
  def itemId: String
  def title: String
  def description: String
}

case class GoogleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  publishedState: String,
  purchaseType: Int,
  autoTranslate: Boolean,
  locale: List[GoogleTranslations],
  autofill: Boolean,
  language: String,
  price: Double

) extends ItemMetadata

case class GoogleTranslations(
  locale: String,
  title: String,
  description: String
)

case class AppleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  productProperties: AppleProductProperties,
  languageProperties: AppleLanguageProperties,
  pricingProperties: ApplePricingProperties,
  durationProperties: AppleDurationProperties

) extends ItemMetadata

case class AppleProductProperties(
  productType: Int, 
  status: String,
  reviewNotes: String
  // screenshot: TODO
)

case class AppleLanguageProperties(
  language: String,
  display: String,
  description: String,
  publicationName: String
)

case class ApplePricingProperties(
  clearedForSale: Boolean,
  price: Double,
  pricingAvailability: PricingAvailability
)

case class PricingAvailability(
  begin: Date,
  end: Date
)

case class AppleDurationProperties(
  autoRenewalDuration: Date,
  freeTrialDuration: Date,
  marketingIncentiveDuration: Date
)

object Item extends ModelCompanion[Item, ObjectId] {

  val dao = new SalatDAO[Item, ObjectId](mongoCollection("applications")){}

  def getDAO = dao
}

package object ItemContext {

  lazy val GoogleMetadataType = "models.application.GoogleMetadata"
  lazy val AppleMetadataType = "models.application.AppleMetadata"

  lazy val GoogleStoreId = 0
  lazy val AppleStoreId = 1

  lazy val VirtualCurrency = 0
  lazy val RealWordCurrency = 1

  implicit def jsonToItem(obj: Option[JsValue]): Option[Item] = {
      obj match {
          case Some(item) => {
              Some(new Item(
                (item \ "_id").as[String],
                (item \ "description").as[String],
                (item \ "store").as[Int],
                (item \ "metadata"),
                (item \ "currency")
              ))
          }
          case None => None 
      }
  }

  implicit def jsonToMetadata(obj: JsValue): ItemMetadata = {
    val metadataType = (obj \ "_t").as[String]
    if(metadataType == GoogleMetadataType){
      new GoogleMetadata(
        (obj \ "osType").as[String],
        (obj \ "itemId").as[String],
        (obj \ "title").as[String],
        (obj \ "description").as[String],
        (obj \ "publishedState").as[String],
        (obj \ "purchaseType").as[Int],
        (obj \ "autoTranslate").as[Boolean],
        (obj \ "locale"),
        (obj \ "autofill").as[Boolean],
        (obj \ "language").as[String],
        (obj \ "price").as[Double]
      )
    } else {
      new AppleMetadata(
        (obj \ "osType").as[String],
        (obj \ "itemId").as[String],
        (obj \ "title").as[String],
        (obj \ "description").as[String],
        (obj \ "productProperties"),
        (obj \ "languageProperties"),
        (obj \ "pricingProperties"),
        (obj \ "languageProperties")
      )
    }
  }

  implicit def jsonToCurrency(obj: JsValue): Currency = {
    new Currency(
      (obj \ "typeOf").as[Int],
      (obj \ "value").as[Double]
    )
  }

  implicit def jsonArrayToLocale(obj: JsValue): List[GoogleTranslations] = {
    obj match {
      case JsArray(array) => {
        array.map((element: JsValue) => {
          new GoogleTranslations(
              (element \ "locale").as[String],
              (element \ "title").as[String],
              (element \ "description").as[String]
          )
        }).toList
      }
      case _ => List[GoogleTranslations]()
    }
  }

  implicit def jsonToAppleProductProperties(obj: JsValue): AppleProductProperties = {
    new AppleProductProperties(
      (obj \ "productType").as[Int],
      (obj \ "status").as[String],
      (obj \ "reviewNotes").as[String]
    )
  }

  implicit def jsonToAppleLanguageProperties(obj: JsValue): AppleLanguageProperties = {
    new AppleLanguageProperties(
      (obj \ "language").as[String],
      (obj \ "display").as[String],
      (obj \ "description").as[String],
      (obj \ "publicationName").as[String]
    )
  }

  implicit def jsonToApplePricingProperties(obj: JsValue): ApplePricingProperties = {
    new ApplePricingProperties(
      (obj \ "clearedForSale").as[Boolean],
      (obj \ "price").as[Double],
      new PricingAvailability(
        (obj \ "pricingAvailability" \ "begin").as[Date],
        (obj \ "pricingAvailability" \ "end").as[Date]
      )
    )
  }

  implicit def jsonToAppleDurationProperties(obj: JsValue): AppleDurationProperties = {
    new AppleDurationProperties(
      (obj \ "autoRenewalDuration").as[Date],
      (obj \ "freeTrialDuration").as[Date],
      (obj \ "marketingIncentiveDuration").as[Date]
    )
  }
}
