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
