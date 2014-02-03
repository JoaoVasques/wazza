package service.aws.modules

import com.tzavellas.sse.guice.ScalaModule
import service.aws.definitions._
import service.aws.implementations._

class AWSModule extends ScalaModule {
  def configure() {
    bind[UploadFileService].to[UploadFileServiceImpl]
    bind[PhotosService].to[PhotosServiceImpl]
  }
}
