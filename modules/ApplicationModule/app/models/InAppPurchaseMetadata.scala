package models.application

import java.util.Date
import play.api.Play.current
import play.api.libs.json._
import scala.language.implicitConversions

trait InAppPurchaseMetadata {
  def osType: String
  def itemId: String
  def title: String
  def description: String
}

case class GoogleTranslations(
  locale: String,
  title: String,
  description: String
)

object GoogleTranslations {
  implicit val reader = (
    (__ \ "locale").read[String] and
    (__ \ "title").read[String] and
    (__ \ "description").read[String]
  )(GoogleTranslations.apply _)

  implicit val write = (
    (__ \ "locale").write[String] and
    (__ \ "title").write[String] and
    (__ \ "description").write[String]
  )(unlift(GoogleTranslations.unapply))
}

case class GoogleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  publishedState: String,
  purchaseType: String,
  autoTranslate: Boolean,
  locale: List[GoogleTranslations],
  autofill: Boolean,
  language: String,
  price: Double,
  countries: List[String]

) extends InAppPurchaseMetadata

object GoogleMetadata {

  implicit val reader = (
    (__ \ "name").read[String] and
    (__ \ "itemId").read[String] and
    (__ \ "title").read[String] and
    (__ \ "description").read[String] and
    (__ \ "publishedState").read[String] and
    (__ \ "purchaseType").read[String] and
    (__ \ "autoTranslate").read[Boolean] and
    (__ \ "locale").read[List[GoogleTranslations]] and
    (__ \ "autofill").read[Boolean] and
    (__ \ "language").read[String] and
    (__ \ "price").read[Double] and
    (__ \ "countries").read[List[String]]
  )(GoogleMetadata.apply _)

  implicit val writer = (
    (__ \ "name").write[String] and
    (__ \ "itemId").write[String] and
    (__ \ "title").write[String] and
    (__ \ "description").write[String] and
    (__ \ "publishedState").write[String] and
    (__ \ "purchaseType").write[String] and
    (__ \ "autoTranslate").write[Boolean] and
    (__ \ "locale").write[List[GoogleTranslations]] and
    (__ \ "autofill").write[Boolean] and
    (__ \ "language").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "countries").write[List[String]]
  )(unlift(GoogleMetadata.unapply))

}

case class AppleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  productProperties: AppleProductProperties,
  languageProperties: AppleLanguageProperties,
  pricingProperties: ApplePricingProperties,
  durationProperties: AppleDurationProperties

) extends InAppPurchaseMetadata

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

object InAppPurchaseMetadata {

  lazy val Android = "android"
  lazy val IOS = "ios"

  implicit object metadataWrite extends Writes[InAppPurchaseMetadata] {
    def writes(iap: InAppPurchaseMetadata) = {
      iap match {
        case google: GoogleMetadata => Json.toJson(google)
        case apple: AppleMetadata => null
      }
    }
  }
  
  implicit object metadataRead extends Reads[InAppPurchaseMetadata] {
    def reads(json: JsValue) = {
      (json \ "osType").as[String] match {
        case Android => json.validate[GoogleMetadata]
        case IOS => null
      }
    }
  }
  
  /**
  def buildJson(metadata: InAppPurchaseMetadata): JsValue = {
    metadata match {
      case google: GoogleMetadata => {
        Json.obj(
          "osType" -> google.osType,
          "itemId" -> google.itemId,
          "title" -> google.title,
          "description" -> google.description,
          "publicationName" -> google.publishedState,
          "purchaseType" -> google.purchaseType,
          "autoTranslate" -> google.autoTranslate,
          "locale" -> Json.toJson(google.locale.map((el: GoogleTranslations) => {
            Json.obj("locale" -> el.locale, "title" -> el.title, "description" -> el.description)
          })),
          "autofill" -> google.autofill,
          "language" -> google.language,
          "price" -> google.price
        )
      }
      case apple: AppleMetadata => {
        //TODO
        Json.obj()
      }
      case _ => null
    }
  }
  **/

  val LanguageCodes = Map(
    "Chinese" ->    "zh_TW",
    "Italian" ->    "it_IT",
    "Czech" ->      "cs_CZ",
    "Japanese" ->   "ja_JP",
    "Danish" ->     "da_DK",
    "Korean" ->     "ko_KR",
    "Dutch" ->      "nl_NL",
    "Norwegian" ->  "no_NO",
    "English" ->    "en_US",
    "Polish" ->     "pl_PL",
    "French" ->     "fr_FR",
    "Portuguese" -> "pt_PT",
    "Finnish" ->    "fi_FI",
    "Russian" ->    "ru_RU",
    "German" ->     "de_DE",
    "Spanish" ->    "es_ES",
    "Hebrew" ->     "iw_IL",
    "Swedish" ->    "sv_SE",
    "Hindi" ->      "hi_IN"
  )
}

package object InAppPurchaseContext {

  import java.util.Date

  // Stores Info
  lazy val GoogleStoreId = 0
  lazy val AppleStoreId = 1
  lazy val GoogleMetadataType = "models.application.GoogleMetadata"
  lazy val AppleMetadataType = "models.application.AppleMetadata"

  // Currency Types
  lazy val VirtualCurrencyType = 0
  lazy val RealWordCurrencyType = 1

  // Purchase Types
  lazy val ManagedProduct = 0
  lazy val Subscription = 1
  lazy val UnManaged = 2

}
