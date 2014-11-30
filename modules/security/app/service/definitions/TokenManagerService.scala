package service.security.definitions

import scala.util.Random
import java.security.SecureRandom
import com.github.nscala_time.time.Imports._

trait TokenManagerService {
  
  type AuthenticityToken = String
  type Id = String
  lazy val CookieName: String = "PLAY2AUTH_SESS_ID"
  protected val tokenSuffix = ":token"
  protected val userIdSuffix = ":userId"
  protected val random = new Random(new SecureRandom())

  private lazy val DefaultCacheExpiration = 7.days.seconds
  lazy val CacheExpiration = play.api.Play.current.configuration.getInt("cache.expiration").getOrElse(DefaultCacheExpiration)

  def startNewSession(userId: Id, timeoutInSeconds: Int = DefaultCacheExpiration): AuthenticityToken

  def remove(token: AuthenticityToken): Unit

  def get(token: AuthenticityToken): Option[String]

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int): Unit
}
