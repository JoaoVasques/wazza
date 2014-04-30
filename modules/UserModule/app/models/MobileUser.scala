package models.user

import org.bson.types.ObjectId
import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileUser(
  userId: String,
  osType: String,
  sessions: List[MobileSession]
)

object MobileUser {

  lazy val KeyId = "userId"

  def getCollection(companyName: String, applicationName: String) = s"$companyName-mUsers-$applicationName"

  implicit val readJson = (
    (__ \ "userId").read[String] and
    (__ \ "osType").read[String] and
    (__ \ "sessions").read[List[MobileSession]]
  )(MobileUser.apply _)

  implicit val buildFromJson = (
    (__ \ "userId").write[String] and
    (__ \ "osType").write[String] and
    (__ \ "sessions").write[List[MobileSession]]
  )(unlift(MobileUser.unapply))
}

