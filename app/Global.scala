import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api.Play
import play.api.Play.current
import service._
import user.service.definitions._
import user.service.implementations._
import user.service.modules._


object Global extends GlobalSettings {
  private lazy val injector = {
    Guice.createInjector(new UserModule)
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }
}