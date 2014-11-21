import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api._
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import service.application.modules._
import service.user.definitions._
import service.user.implementations._
import service.user.modules._
import service.security.modules._
import service.aws.modules._
import service.persistence.modules.PersistenceModule
import service.analytics.modules.AnalyticsModule
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.Props
import scala.concurrent.duration._

object Global extends GlobalSettings {
  
  private lazy val injector = {
    Guice.createInjector(
      new AppModule,
      new UserModule,
      new SecurityModule,
      new AWSModule,
      new PersistenceModule,
      new AnalyticsModule
    )
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.index()
    ))
  }
}

