package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import scala.language.implicitConversions
import play.api.libs.functional.syntax._

case class VirtualCurrency(
  name: String,
  price: Double,
  inAppPurchaseMetadata: InAppPurchaseMetadata
)

object VirtualCurrency {
  val Id = "name"

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

  implicit def buildVCListFromJsonArray(array: JsArray): List[VirtualCurrency] = {
   array.value .map((json: JsValue) => {
      new VirtualCurrency(
        (json \ "name").as[String],
        (json \ "price").as[Double],
        (json \ "inAppPurchaseMetadata")
      )
    }).toList
  }
}
