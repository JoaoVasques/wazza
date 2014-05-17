package models.security

import play.api.Play.current
import play.api.libs.json._
import scala.language.implicitConversions
import play.api.libs.functional.syntax._

case class CompanyData(
  name: String,
  apps: List[String]
)

object CompanyData {

  def collection = "companiesData"

  implicit val reader = (
    (__ \ "name").read[String] and
    (__ \ "apps").read[List[String]]
  )(CompanyData.apply _)

  implicit val write = (
    (__ \ "name").write[String] and
    (__ \ "apps").write[List[String]]
  )(unlift(CompanyData.unapply))
}
