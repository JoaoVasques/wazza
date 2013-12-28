import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api.Play
import play.api.Play.current
import service.application.modules._
import service.user.definitions._
import service.user.implementations._
import service.user.modules._
import service.security.modules._

object Global extends GlobalSettings {
  
  private lazy val injector = {
    Guice.createInjector(
      new AppModule,
      new UserModule,
      new SecurityModule
    )
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }
}
