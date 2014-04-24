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

  def getCollection(companyName: String, applicationName: String) = s"$companyName-mobileSessions-$applicationName"

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
