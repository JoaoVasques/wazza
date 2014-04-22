package models.user

import org.bson.types.ObjectId
import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileUser(
  dbId: Long,
  userId: String,
  osType: String,
  sessions: List[MobileSession],
  purchases: List[PurchaseInfo]
)

object MobileUser {

  lazy val KeyId = "userId"

  def getCollection(companyName: String, applicationName: String) = s"$companyName-mUsers-$applicationName"

  implicit val readJson = (
    (__ \ "dbId").read[Long] and
    (__ \ "userId").read[String] and
    (__ \ "osType").read[String] and
    (__ \ "sessions").read[List[MobileSession]] and
    (__ \ "purchases").read[List[PurchaseInfo]]
  )(MobileUser.apply _)

  implicit val buildFromJson = (
    (__ \ "dbId").write[Long] and
    (__ \ "userId").write[String] and
    (__ \ "osType").write[String] and
    (__ \ "sessions").write[List[MobileSession]] and
    (__ \ "purchases").write[List[PurchaseInfo]]
  )(unlift(MobileUser.unapply))
}

