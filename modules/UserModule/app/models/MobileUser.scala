package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileUser(
  userId: String,
  osType: String,
  sessions: List[MobileSession],
  purchases: List[PurchaseInfo]
)

object MobileUser {

  lazy val MobileUserCollection = "mobileUsers"

  implicit val readJson = (
    (__ \ "userId").read[String] and
    (__ \ "osType").read[String] and
    (__ \ "sessions").read[List[MobileSession]] and
    (__ \ "purchases").read[List[PurchaseInfo]]
  )(MobileUser.apply _)

  implicit val buildFromJson = (
    (__ \ "userId").write[String] and
    (__ \ "osType").write[String] and
    (__ \ "sessions").write[List[MobileSession]] and
    (__ \ "purchases").write[List[PurchaseInfo]]
  )(unlift(MobileUser.unapply))
}

