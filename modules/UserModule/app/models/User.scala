package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class User(
  name: String,
  email: String,
  var password: String,
  company: String,
  applications: List[String]
)

object User {

  def getCollection() = "company-user"

  implicit val userReadJson = (
    (__ \ "name").read[String] and
    (__ \ "email").read[String] and
    (__ \ "password").read[String] and
    (__ \ "company").read[String] and
    (__ \ "applications").read[List[String]]
  )(User.apply _)

  implicit val userBuildFromJson = (
    (__ \ "name").write[String] and
    (__ \ "email").write[String] and
    (__ \ "password").write[String] and
    (__ \ "company").write[String] and
    (__ \ "applications").write[List[String]]
  )(unlift(User.unapply))
}

