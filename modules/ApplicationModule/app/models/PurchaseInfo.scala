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
import play.api.libs.functional.syntax._

case class PurchaseInfo(
  id: String,
  applicationName: String,
  itemId: String,
  price: Double,
  time: String,
  location: Option[LocationInfo]
)

case class LocationInfo(
  latitude: Double,
  longitude: Double
)

object LocationInfo {
  implicit val locationJsonRead  = (
    (__ \ "latitude").read[Double] and
      (__ \ "longitude").read[Double]
  )(LocationInfo.apply _)
}
