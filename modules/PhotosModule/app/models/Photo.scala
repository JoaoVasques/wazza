package models.photos

import play.api.Play.current
import se.radley.plugin.salat._
import PhotosContext._

object Photo {
  
  lazy val photosStorage = gridFS("photos")
}
