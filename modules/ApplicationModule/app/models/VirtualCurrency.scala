package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import ApplicationMongoContext._
import InAppPurchaseContext._
import scala.language.implicitConversions
import play.api.libs.functional.syntax._

case class VirtualCurrency(
  name: String,
  price: Double,
  inAppPurchaseMetadata: InAppPurchaseMetadata
)

object VirtualCurrency {

  implicit val reader = (
    (__ \ "name").read[String] and
    (__ \ "price").read[Double] and
    (__ \ "inAppPurchaseMetadata").read[InAppPurchaseMetadata]
  )(VirtualCurrency.apply _)

  implicit val writer = (
    (__ \ "name").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "inAppPurchaseMetadata").write[InAppPurchaseMetadata]
  )(unlift(VirtualCurrency.unapply))
/**
  def buildJson(vc: VirtualCurrency): JsValue = {
    Json.obj(
      "name" -> vc.name,
      "price" -> vc.price,
      "inAppPurchaseMetadata" -> InAppPurchaseMetadata.buildJson(vc.inAppPurchaseMetadata),
      "elementId" -> "name",
      "attributeName" -> "virtualCurrencies"
    )
  }

  implicit def buildFromJson(json: Option[JsValue]): Option[VirtualCurrency] = {
    json match {
      case Some(vc) => {
        Some(new VirtualCurrency(
          (vc \ "name").as[String],
          (vc \ "price").as[Double],
          (vc \ "inAppPurchaseMetadata")
        ))
      }
      case None => None
    }
  }

  implicit def convertJsonSetToVCList(set: Set[JsValue]): List[VirtualCurrency] = {
    set.map((json: JsValue) => {
      new VirtualCurrency(
        (json \ "name").as[String],
        (json \ "price").as[Double],
        (json \ "inAppPurchaseMetadata")
      )
    }).toList
  }
  **/
}
