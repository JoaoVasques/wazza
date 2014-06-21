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
  sessionId: String,
  userId: String,
  applicationName: String,
  itemId: String,
  price: Double,
  time: String,
  deviceInfo: DeviceInfo,
  location: Option[LocationInfo]
)

object PurchaseInfo {

  lazy val Id = "id"
  lazy val UserId = "userId"
  def getRecommendationCollection(companyName: String, applicationName: String) = s"${companyName}_recommendation_${applicationName}"
  def getCollection(companyName: String, applicationName: String) = s"${companyName}_purchases_${applicationName}"

  implicit def buildJsonFromMap(map: Map[String, JsValue]): JsValue = {
    Json.toJson(
      Map(
        "id" -> map("id"),
        "sessionId" -> map("sessionId"),
        "userId" -> map("userId"),
        "name" -> map("name"),
        "itemId" -> map("itemId"),
        "price" -> map("price"),
        "deviceInfo" -> map("deviceInfo"),
        "time" -> map("time")
      )
    )
  }

  implicit val reader = (
    (__ \ "id").read[String] and
    (__ \ "sessionId").read[String] and
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
    (__ \ "sessionId").write[String] and
    (__ \ "userId").write[String] and
    (__ \ "name").write[String] and
    (__ \ "itemId").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "time").write[String] and
    (__ \ "deviceInfo").write[DeviceInfo] and
    (__ \ "location").writeNullable[LocationInfo]
  )(unlift(PurchaseInfo.unapply))
}

