package service.modules

import com.tzavellas.sse.guice.ScalaModule
import service.definitions._
import service.implementations._

class ApplicationModule extends ScalaModule {
  def configure() {
    bind[ApplicationService].to[ApplicationServiceImpl]
  }
}