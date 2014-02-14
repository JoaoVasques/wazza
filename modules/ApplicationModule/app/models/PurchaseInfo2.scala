package models.application

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class LocationInfo2(
  latitude: Double,
  longitude: Double
)

object LocationInfo2 {
  implicit val reader  = (
    (__ \ "latitude").read[Double] and
      (__ \ "longitude").read[Double]
  )(LocationInfo2.apply _)

  implicit val write = (
    (__ \ "latitude").write[Double] and
    (__ \ "longitude").write[Double]
  )(unlift(LocationInfo2.unapply))
}

case class PurchaseInfo2(
  id: String,
  applicationName: String,
  itemId: String,
  price: Double,
  time: String,
  location: Option[LocationInfo2]
)

object PurchaseInfo2 {

  lazy val PurchaseCollection = "purchases"

  implicit val reader = (
    (__ \ "id").read[String] and
    (__ \ "name").read[String] and
    (__ \ "itemId").read[String] and
    (__ \ "price").read[Double] and
    (__ \ "time").read[String] and
    (__ \ "location").readNullable[LocationInfo2]
  )(PurchaseInfo2.apply _)

  implicit val writes = (
    (__ \ "id").write[String] and
    (__ \ "name").write[String] and
    (__ \ "itemId").write[String] and
    (__ \ "price").write[Double] and
    (__ \ "time").write[String] and
    (__ \ "location").writeNullable[LocationInfo2]
  )(unlift(PurchaseInfo2.unapply))
}

