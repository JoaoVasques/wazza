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

case class Item(
  @Key("_id") name: String,
  description: String,
  store: Int,
  metadata: InAppPurchaseMetadata,
  currency: Currency,
  imageInfo: ImageInfo,
  override val elementId: String = "_id",
  override val attributeName: String = "items"
) extends ApplicationList

case class Currency(
  typeOf: Int, //virtual or real money
  value: Double,
  virtualCurrency: Option[String]
)

case class ImageInfo(
  name: String,
  url: String
)

object Item extends ModelCompanion[Item, ObjectId] {

  val dao = new SalatDAO[Item, ObjectId](mongoCollection("applications")){}

  def getDAO = dao

  implicit def buildFromJson(obj: Option[JsValue]): Option[Item] = {
    obj match {
      case Some(item) => {
        Some(new Item(
          (item \ "_id").as[String],
          (item \ "description").as[String],
          (item \ "store").as[Int],
          (item \ "metadata"),
          (item \ "currency"),
          (item \ "imageInfo")
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
}
