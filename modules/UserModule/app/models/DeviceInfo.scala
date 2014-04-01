package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

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

