package models.application

import play.api.libs.json._
import scala.language.implicitConversions
import play.api.libs.functional.syntax._

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

  implicit def convertToJson(item: Item): JsObject = {
    Json.obj(
      "name" -> item.name,
      "description" -> item.description,
      "store" -> item.store,
      "metadata" -> InAppPurchaseMetadata.buildJson(item.metadata),
      "currency" -> Json.toJson(item.currency),
      "imageInfo" -> Json.toJson(item.imageInfo)
    )
  }

  implicit def buildFromJson(json: JsValue): Item = {
    new Item(
      (json \ "name").as[String],
      (json \ "description").as[String],
      (json \ "store").as[Int],
      (json \ "metadata"),
      (json \ "currency").validate[Currency].fold(
        valid = {c => c},
        invalid = {_ => null}
      ),
      (json \ "imageInfo").validate[ImageInfo].fold(
        valid = {i => i},
        invalid = {_ => null}
      )
    )
  }

  implicit def buildFromJsonOption(obj: Option[JsValue]): Option[Item] = {
    obj match {
      case Some(item) => {
        Some(this.buildFromJson(item))
      }
      case None => None
    }
  }

  implicit def buildItemListFromJsArray(array: JsArray): List[Item] = {
    array.value.map{(el: JsValue) =>
      this.buildFromJson(el)
    }.toList
  }

  implicit def imageInfoFromJson(json: JsValue): ImageInfo = {
    new ImageInfo(
      (json \ "name").as[String],
      (json \ "url").as[String]
    )
  }
}

