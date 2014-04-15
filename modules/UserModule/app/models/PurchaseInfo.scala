package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class LocationInfo(
  latitude: Double,
  longitude: Double
)

object LocationInfo {
  implicit val reader  = (
    (__ \ "latitude").read[Double] and
      (__ \ "longitude").read[Double]
  )(LocationInfo.apply _)

  implicit val write = (
    (__ \ "latitude").write[Double] and
    (__ \ "longitude").write[Double]
  )(unlift(LocationInfo.unapply))
}

/**
  Purchase Id format: Hash(appName + itemID + time + device)
**/
case class PurchaseInfo(
  id: String,
  userId: String,
  applicationName: String,
  itemId: String,
  price: Double,
  time: String,
  deviceInfo: DeviceInfo,
  location: Option[LocationInfo]
)

object PurchaseInfo {

  def getCollection(companyName: String, applicationName: String) = s"$companyName-purchases-$applicationName"

  implicit val reader = (
    (__ \ "id").read[String] and
    (__ \ "userId").read[String] and
    (__ \ "name").read[String] and
    (__ \ "itemId").read[String] and
    (__ \ "price").read[Double] and
    (__ \ "time").read[String] and
    (__ \ "deviceInfo").read[DeviceInfo] and
    (__ \ "location").readNullable[LocationInfo]
  )(PurchaseInfo.apply _)

  implicit val writes = (
    (__ \ "id").write[String] and
    (__ \ "userId").write[String] and
    (__ \ "name").write[String] and
    (__ \ "itemId").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "time").write[String] and
    (__ \ "deviceInfo").write[DeviceInfo] and
    (__ \ "location").writeNullable[LocationInfo]
  )(unlift(PurchaseInfo.unapply))
}

