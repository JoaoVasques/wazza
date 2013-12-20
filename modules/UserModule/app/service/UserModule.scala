package user.service

import com.tzavellas.sse.guice.ScalaModule

class UserModule extends ScalaModule {
  def configure() {
    bind[UserService].to[UserServiceImpl]
  }
}