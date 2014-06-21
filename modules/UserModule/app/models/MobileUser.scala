package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class MobileUser(userId: String)

object MobileUser {

  lazy val KeyId = "userId"

  def getCollection(companyName: String, applicationName: String) = s"${companyName}_mUsers_${applicationName}"

  implicit def readJson(mobileUser: MobileUser): JsValue = {
    Json.obj(
      "userId" -> mobileUser.userId
    )
  }

  implicit def buildFromJson(json: JsValue): MobileUser = {
    new MobileUser(
      (json \ "userId").as[String]
    )
  }
}

