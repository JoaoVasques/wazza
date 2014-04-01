package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileUser(
  userId: String,
  osType: String,
  sessions: List[MobileSession],
  purchases: List[String]
)

object MobileUser {

  implicit val readJson = (
    (__ \ "userId").read[String] and
    (__ \ "osType").read[String] and
    (__ \ "sessions").read[List[MobileSession]] and
    (__ \ "purchases").read[List[String]]
  )(MobileUser.apply _)

  implicit val buildFromJson = (
    (__ \ "userId").write[String] and
    (__ \ "osType").write[String] and
    (__ \ "sessions").write[List[MobileSession]] and
    (__ \ "purchases").write[List[String]]
  )(unlift(MobileUser.unapply))
}

