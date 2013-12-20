package user.service.modules

import com.tzavellas.sse.guice.ScalaModule
import user.service.definitions._
import user.service.implementations._

class UserModule extends ScalaModule {
  def configure() {
    bind[UserService].to[UserServiceImpl]
  }
}