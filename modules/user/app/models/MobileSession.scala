package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import models.common._

case class MobileSession(
  id: String, //hash
  userId: String,
  length: Double,
  startTime: Date,
  deviceInfo: DeviceInfo,
  purchases: List[String] //List of purchases id's
)

object MobileSession {

  val Id = "id"
  val Purchases = "purchases"

  def getCollection(companyName: String, applicationName: String) = s"${companyName}_mobileSessions_${applicationName}"

  implicit def buildFromJson(json: JsValue): MobileSession = {
    new MobileSession(
      (json \ "id").as[String],
      (json \ "userId").as[String],
      (json \ "length").as[Double],
      new Date((json \ "startTime").as[Long]),
      (json \ "device").validate[DeviceInfo].asOpt.get,
      (json \ "purchases").as[List[String]]
    )
  }

  implicit def toJson(session: MobileSession): JsValue = {
    Json.obj(
      "id" -> session.id,
      "userId" -> session.userId,
      "length" -> session.length,
      "startTime" -> session.startTime.getTime,
      "device" -> Json.toJson(session.deviceInfo),
      "purchases" -> session.purchases
    )
  }
}

