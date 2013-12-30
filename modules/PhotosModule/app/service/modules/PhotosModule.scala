package service.photos.modules

import com.tzavellas.sse.guice.ScalaModule
import service.photos.definitions._
import service.photos.implementations._

class PhotosModule extends ScalaModule {
  def configure() {
    bind[UploadPhotoService].to[UploadPhotoServiceImpl]
  }
}
