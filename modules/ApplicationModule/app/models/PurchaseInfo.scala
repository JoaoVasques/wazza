package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import PurchaseMongoContext._
import InAppPurchaseContext._
import scala.language.implicitConversions

case class PurchaseInfo(
  @Key("_id") id: String,
  itemId: String,
  price: Double,
  time: String,
  location: LocationInfo
)

case class LocationInfo(
  latitude: Double,
  longitude: Double
)

object PurchaseInfo extends ModelCompanion[PurchaseInfo, ObjectId] {
  val dao = new SalatDAO[PurchaseInfo, ObjectId](mongoCollection("purchases")){}
  def getDAO = dao
}
