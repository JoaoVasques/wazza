package service.security.definitions

import play.api.cache.Cache
import play.api.Play._

class TokenStoreServiceImpl extends TokenStoreService{

  def put(token: TokenId, userId: UserId, timeoutInSeconds: Int) = {
    Cache.set(token, userId, timeoutInSeconds)
  }

  def remove[T <: String](element: T): Unit = {
    Cache.remove(element)
  }

  def get(token: AuthenticityToken): Option[UserId] = {
    Cache.get(token).map(_.asInstanceOf[UserId])
  }
}
