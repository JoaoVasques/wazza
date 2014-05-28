package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileSession(
  id: String, //hash
  userId: String,
  length: Double,
  startTime: String,
  deviceInfo: DeviceInfo,
  purchases: List[String] //List of purchases id's
)

object MobileSession {

  val Id = "id"
  val Purchases = "purchases"

  def getCollection(companyName: String, applicationName: String) = s"${companyName}_mobileSessions_${applicationName}"

  implicit def buildJsonFromMap(map: Map[String, JsValue]): JsValue = {
    Json.toJson(
      Map(
        "id" -> map("id"),
        "userId" -> map("userId"),
        "deviceInfo" -> map("deviceInfo"),
        "startTime" -> map("startTime"),
        "sessionLength" -> map("sessionLength"),
        "purchases" -> map("purchases")
      )
    )
  }

  implicit val readJson = (
    (__ \ "id").read[String] and
    (__ \ "userId").read[String] and
    (__ \ "sessionLength").read[Double] and
    (__ \ "startTime").read[String] and
    (__ \ "deviceInfo").read[DeviceInfo] and
    (__ \ "purchases").read[List[String]]
  )(MobileSession.apply _)

  implicit val buildFromJson = (
    (__ \ "id").write[String] and
    (__ \ "userId").write[String] and
    (__ \ "sessionLength").write[Double] and
    (__ \ "startTime").write[String] and
    (__ \ "deviceInfo").write[DeviceInfo] and
    (__ \ "purchases").write[List[String]]
  )(unlift(MobileSession.unapply))

}
