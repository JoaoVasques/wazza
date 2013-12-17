import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api.Play
import play.api.Play.current
import service._


object Global extends GlobalSettings {
  private lazy val injector = {
    Play.isDev match {
      case true => Guice.createInjector(new ProdModule)
      case false => Guice.createInjector(new DevModule)
    }
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }
}