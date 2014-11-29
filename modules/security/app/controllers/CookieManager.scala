package controllers.security

import play.api._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global

trait CookieManager { self: Controller =>

  type AuthenticityToken = String

  private val AuthTokenHeader = "X-XSRF-TOKEN"
  private val AuthTokenCookieKey = "XSRF-TOKEN"
  private val AuthTokenUrlKey = "auth"

  implicit class ResultWithToken(result: Result) {

    def withToken(token: String): Result = {
      result.withCookies(Cookie(AuthTokenCookieKey, token, None, httpOnly = false))
    }

    def discardingToken(token: String)(removeToken: AuthenticityToken => Unit): Result = {
      removeToken(token)
      result.discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
    }
  }
}

