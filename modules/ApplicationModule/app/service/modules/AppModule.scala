package service.application.modules

import com.tzavellas.sse.guice.ScalaModule
import service.application.definitions._
import service.application.implementations._

class AppModule extends ScalaModule {
  def configure() {
    bind[ApplicationService].to[ApplicationServiceImpl]
    bind[ItemService].to[ItemServiceImpl]
  }
}
