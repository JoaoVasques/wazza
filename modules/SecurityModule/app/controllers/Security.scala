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

trait Security { self: Controller =>

  type AuthenticityToken = String

  lazy private val tokenService = Guice.createInjector(new SecurityModule).getInstance(classOf[TokenManagerService])

  implicit val app: play.api.Application = play.api.Play.current
  private lazy val DefaultCacheExpiration = 7.days.seconds
  lazy val CacheExpiration = app.configuration.getInt("cache.expiration").getOrElse(DefaultCacheExpiration)

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"

  /** Checks that a token is either in the header or in the query string */
  def HasToken[A](p: BodyParser[A] = parse.anyContent)(f: String => String => Request[A] => Result): Action[A] =
    Action(p) { implicit request =>
      val maybeToken = request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))
      maybeToken flatMap { token =>
        tokenService.get(token.filter(_ != '"')) map { userid =>
          f(token)(userid)(request)
        }
      } getOrElse Forbidden(Json.obj("err" -> "No Token"))
    }

  implicit class ResultWithToken(result: SimpleResult) {
    def withToken(token: String): SimpleResult = {
      result.withCookies(Cookie(AuthTokenCookieKey, token, None, httpOnly = false))
    }

    def discardingToken(token: String)(removeToken: AuthenticityToken => Unit): SimpleResult = {
      removeToken(token)
      result.discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
    }
  }
}
