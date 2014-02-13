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
import InAppPurchaseContext._
import scala.language.implicitConversions

case class VirtualCurrency(
  name: String,
  price: Double,
  inAppPurchaseMetadata: InAppPurchaseMetadata,
  override val elementId: String = "name",
  override val attributeName: String = "virtualCurrencies"
) extends ApplicationList

object VirtualCurrency extends ModelCompanion[VirtualCurrency, ObjectId] {

  val dao = new SalatDAO[VirtualCurrency, ObjectId](mongoCollection("applications")){}

  def getDAO = dao

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
}
