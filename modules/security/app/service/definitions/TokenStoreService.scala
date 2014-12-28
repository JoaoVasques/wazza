package service.security.definitions

trait TokenStoreService {

  type TokenId = String
  type UserId = String
  type AuthenticityToken = String

  protected val tokenSuffix = ":token"
  protected val userIdSuffix = ":userId"

  def put(token: TokenId, userId: UserId, timeoutInSeconds: Int): Unit

  def remove[T <: String](element: T): Unit

  def get(token: AuthenticityToken): Option[UserId]
}
