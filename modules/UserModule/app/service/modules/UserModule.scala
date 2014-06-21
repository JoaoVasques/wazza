package service.user.modules

import com.tzavellas.sse.guice.ScalaModule
import service.user.definitions._
import service.user.implementations._

class UserModule extends ScalaModule {
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[MobileUserService].to[MobileUserServiceImpl]
    bind[PurchaseService].to[PurchaseServiceImpl]
    bind[MobileSessionService].to[MobileSessionServiceImpl]
  }
}
