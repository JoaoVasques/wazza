package models.application

import play.api.libs.json._
import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import InAppPurchaseContext._

case class Currency(
  typeOf: Int, //virtual or real money
  value: Double,
  virtualCurrency: Option[String]
)

object Currency {

  implicit val reader = (
    (__ \ "typeOf").read[Int] and
    (__ \ "value").read[Double] and
    (__ \ "virtualCurrency").readNullable[String]
  )(Currency.apply _)

  implicit val writer = (
    (__ \ "typeOf").write[Int] and
    (__ \ "value").write[Double] and
    (__ \ "virtualCurrency").writeNullable[String]
  )(unlift(Currency.unapply))
}

case class ImageInfo(
  name: String,
  url: String
)

object ImageInfo {

  implicit val reader = (
    (__ \ "name").read[String] and
    (__ \ "url").read[String]
  )(ImageInfo.apply _)

  implicit val writer = (
    (__ \ "name").write[String] and
    (__ \ "url").write[String]
  )(unlift(ImageInfo.unapply))
}

case class Item(
  name: String,
  description: String,
  store: Int,
  metadata: InAppPurchaseMetadata,
  currency: Currency,
  imageInfo: ImageInfo
)

object Item  {

  lazy val ElementId = "name"
  lazy val AttributeName = "items"

  implicit val reader = (
    (__ \ "name").read[String] and
    (__ \ "description").read[String] and
    (__ \ "store").read[Int] and
    (__ \ "metadata").read[InAppPurchaseMetadata] and
    (__ \ "currency").read[Currency] and
    (__ \ "imageInfo").read[ImageInfo]
  )(Item.apply _)

  implicit val writer = (
    (__ \ "name").write[String] and
      (__ \ "description").write[String] and
      (__ \ "store").write[Int] and
      (__ \ "metadata").write[InAppPurchaseMetadata] and
      (__ \ "currency").write[Currency] and
      (__ \ "imageInfo").write[ImageInfo]

  )(unlift(Item.unapply))
    
  /**
  implicit def buildFromJson(obj: Option[JsValue]): Option[Item] = {
    obj match {
      case Some(item) => {
        Some(new Item(
          (item \ "description").as[String],
          (item \ "store").as[Int],
          (item \ "metadata"),
          (item \ "currency").validate[Currency],
          (item \ "imageInfo").validate[ImageInfo]
        ))
      }
      case None => None 
    }
  }

  implicit def imageInfoFromJson(json: JsValue): ImageInfo = {
    new ImageInfo(
      (json \ "name").as[String],
      (json \ "url").as[String]
    )
  }
    * */
}

