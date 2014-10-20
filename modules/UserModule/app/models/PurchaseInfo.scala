package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.util.Date

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
  sessionId: String,
  userId: String,
  itemId: String,
  price: Double,
  time: Date,
  deviceInfo: DeviceInfo,
  location: Option[LocationInfo]
)

object PurchaseInfo {

  lazy val Id = "id"
  lazy val UserId = "userId"
  def getCollection(companyName: String, applicationName: String) = s"${companyName}_purchases_${applicationName}"

  implicit def buildJsonFromMap(map: Map[String, JsValue]): JsValue = {
    Json.toJson(
      Map(
        "id" -> map("id"),
        "sessionId" -> map("sessionId"),
        "userId" -> map("userId"),
        "itemId" -> map("itemId"),
        "price" -> map("price"),
        "time" -> map("time"),
        "deviceInfo" -> map("deviceInfo")
      )
    )
  }

  implicit val reader = (
    (__ \ "id").read[String] and
    (__ \ "sessionId").read[String] and
    (__ \ "userId").read[String] and
    (__ \ "itemId").read[String] and
    (__ \ "price").read[Double] and
    (__ \ "time").read[Date] and
    (__ \ "deviceInfo").read[DeviceInfo] and
    (__ \ "location").readNullable[LocationInfo]
  )(PurchaseInfo.apply _)

  implicit val writes = (
    (__ \ "id").write[String] and
    (__ \ "sessionId").write[String] and
    (__ \ "userId").write[String] and
    (__ \ "itemId").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "time").write[Date] and
    (__ \ "deviceInfo").write[DeviceInfo] and
    (__ \ "location").writeNullable[LocationInfo]
  )(unlift(PurchaseInfo.unapply))
}

