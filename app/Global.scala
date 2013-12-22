import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api.Play
import play.api.Play.current
import service._
import service.modules._
import service.user.definitions._
import service.user.implementations._
import service.user.modules._

object Global extends GlobalSettings {
  private lazy val injector = {
    Guice.createInjector(new ApplicationModule)
    Guice.createInjector(new UserModule)
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }
}
