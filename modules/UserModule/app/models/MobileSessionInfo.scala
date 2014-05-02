package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

/**
  TODO - description
**/

case class MobileSessionInfo(
  hash: String,
  userId: String,
  applicationName: String,
  companyName: String
)

object MobileSessionInfo {

  def Id = "hash"
  def collection = "MobileSessionHashTable"

  implicit val readJson = (
    (__ \ "hash").read[String] and
    (__ \ "userId").read[String] and
    (__ \ "applicationName").read[String] and
    (__ \ "companyName").read[String]
  )(MobileSessionInfo.apply _)

  implicit val buildFromJson = (
    (__ \ "hash").write[String] and
    (__ \ "userId").write[String] and
    (__ \ "applicationName").write[String] and
    (__ \ "companyName").write[String]
  )(unlift(MobileSessionInfo.unapply))

}
