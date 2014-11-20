package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._

case class Buyer(userId: String)

object Buyer {

  val Id = "userId"

  def getCollection(companyName: String, applicationName: String) = s"${companyName}_Buyers_${applicationName}"
}

