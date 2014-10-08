package controllers.security

import play.api._
import play.api.mvc._

import play.api.cache._
import play.api.libs.json._
import play.api.data._
import play.api.mvc.Results._
import play.api.mvc.BodyParsers.parse
import com.github.nscala_time.time.Imports._
import java.security.SecureRandom
import scala.annotation.tailrec
import com.google.inject._
import service.security.modules.{SecurityModule}
import service.security.definitions.{TokenManagerService}
import scala.concurrent._
import ExecutionContext.Implicits.global

private[security] class UserRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

private[security] case class UserAction[A](action: Action[A]) extends Action[A] {

  private type AuthenticityToken = String

  lazy private val tokenService = Guice.createInjector(new SecurityModule)
    .getInstance(classOf[TokenManagerService])

  implicit val app: play.api.Application = play.api.Play.current
  private lazy val DefaultCacheExpiration = 7.days.seconds
  lazy val CacheExpiration = app.configuration.getInt("cache.expiration").getOrElse(DefaultCacheExpiration)

  private val AuthTokenHeader = "X-XSRF-TOKEN"
  private val AuthTokenCookieKey = "XSRF-TOKEN"
  private val AuthTokenUrlKey = "auth"

  lazy val parser = action.parser

  def apply(request: Request[A]): Future[SimpleResult] = {
    val maybeToken = request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))
    maybeToken flatMap {token =>
      tokenService.get(token.filter(_ != '"'))
    } match {
      case Some(userId) => action(new UserRequest(userId, request))
      case _ => Future.successful(BadRequest("user not logged in"))
    }
  }
}

object UserAuthenticationAction extends ActionBuilder[UserRequest] {

  def invokeBlock[A](request: Request[A], block: (UserRequest[A] => Future[SimpleResult])) = {
    request match {
      case req: UserRequest[A] => block(req)
      case _ => Future.successful(BadRequest("Invalid Request"))
    }
  }

  override def composeAction[A](action: Action[A]) = UserAction(action)
}

