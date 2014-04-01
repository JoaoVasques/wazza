package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileSession(
  length: Double,
  startTime: String,
  deviceInfo: DeviceInfo
  //location
)

case class DeviceInfo(
  osType: String,
  name: String,
  version: String,
  model: String
)

object DeviceInfo {

  implicit val readJson = (
    (__ \ "osType").read[String] and
    (__ \ "osName").read[String] and
    (__ \ "osVersion").read[String] and
    (__ \ "deviceModel").read[String]
  )(DeviceInfo.apply _)

  implicit val buildFromJson = (
    (__ \ "osType").write[String] and
    (__ \ "osName").write[String] and
    (__ \ "osVersion").write[String] and
    (__ \ "deviceModel").write[String]
  )(unlift(DeviceInfo.unapply))
}

object MobileSession {

  implicit val readJson = (
    (__ \ "sessionLength").read[Double] and
    (__ \ "startTime").read[String] and
    (__ \ "deviceInfo").read[DeviceInfo]
  )(MobileSession.apply _)

  implicit val buildFromJson = (
    (__ \ "sessionLength").write[Double] and
    (__ \ "startTime").write[String] and
    (__ \ "deviceInfo").write[DeviceInfo]
  )(unlift(MobileSession.unapply))

}
