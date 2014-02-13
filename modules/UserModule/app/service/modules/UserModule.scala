package service.user.modules

import com.tzavellas.sse.guice.ScalaModule
import service.user.definitions._
import service.user.implementations._

class UserModule extends ScalaModule {
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[UserService2].to[UserService2Impl]
  }
}
