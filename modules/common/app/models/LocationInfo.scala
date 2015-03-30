package models.common

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class LocationInfo(
  latitude: Double,
  longitude: Double
)

object LocationInfo {
  implicit val reader  = (
    (__ \ "latitude").read[Double] and
      (__ \ "longitude").read[Double]
  )(LocationInfo.apply _)

  implicit val write = (
    (__ \ "latitude").write[Double] and
      (__ \ "longitude").write[Double]
  )(unlift(LocationInfo.unapply))
}
