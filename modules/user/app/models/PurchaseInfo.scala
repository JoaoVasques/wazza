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

  implicit def buildFromJson(json: JsValue): PurchaseInfo = {
    def getLocation = {
      if(json.as[JsObject].keys.contains("location")) {
        (json \ "location").validate[LocationInfo].asOpt
      } else {
        None
      }
    }
    new PurchaseInfo(
      (json \ "id").as[String],
      (json \ "sessionId").as[String],
      (json \ "userId").as[String],
      (json \ "itemId").as[String],
      (json \ "price").as[Double],
      new Date((json \ "time").as[Long]),
      (json \ "device").validate[DeviceInfo].asOpt.get,
      getLocation
    )
  }

  implicit def toJson(purchase: PurchaseInfo): JsValue = {
    purchase.location match {
      case Some(_) => {
        Json.obj(
          "id" -> purchase.id,
          "sessionId" -> purchase.sessionId,
          "userId" -> purchase.userId,
          "itemId" -> purchase.itemId,
          "price" -> purchase.price,
          "time" -> purchase.time.getTime,
          "device" -> Json.toJson(purchase.deviceInfo),
          "location" -> Json.toJson(purchase.location)
        )
      }
      case _ => {
        Json.obj(
          "id" -> purchase.id,
          "sessionId" -> purchase.sessionId,
          "userId" -> purchase.userId,
          "itemId" -> purchase.itemId,
          "price" -> purchase.price,
          "time" -> purchase.time.getTime,
          "device" -> Json.toJson(purchase.deviceInfo)
        )
      }
    }
  }
}

