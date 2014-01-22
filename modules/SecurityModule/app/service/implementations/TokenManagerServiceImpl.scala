package service.security.implementations

import scala.annotation.tailrec
import service.security.definitions._
import com.google.inject._

class TokenManagerServiceImpl @Inject()(
  tokenStoreService: TokenStoreService
) extends TokenManagerService{

  def startNewSession(userId: Id, timeoutInSeconds: Int): AuthenticityToken = {
    removeByUserId(userId)
    val token = generate
    store(token, userId, timeoutInSeconds)
    token
  }

  def remove(token: AuthenticityToken): Unit = {
    get(token) foreach unsetUserId
    unsetToken(token)
  }

  def get(token: AuthenticityToken): Option[String] = {
    tokenStoreService.get(token + tokenSuffix)
  }

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int): Unit = {
    get(token).foreach(store(token, _, timeoutInSeconds))
  }

  private def unsetToken(token: AuthenticityToken) {
    tokenStoreService.remove(token + tokenSuffix)
  }

  private def unsetUserId(userId: Id) {
    tokenStoreService.remove(userId.toString + userIdSuffix)
  }
  
  private def store(token: AuthenticityToken, userId: Id, timeoutInSeconds: Int) {
    tokenStoreService.put(token + tokenSuffix, userId, timeoutInSeconds)
    tokenStoreService.put(userId.toString + userIdSuffix, token, timeoutInSeconds)
  }

  @tailrec
  private final def generate: AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.!~*'()"
    val token = Stream.continually(random.nextInt(table.size)).map(table).take(64).mkString
    if (get(token).isDefined) generate else token
  }

  private def removeByUserId(userId: Id) {
    tokenStoreService.get(userId.toString + userIdSuffix) foreach unsetToken
    unsetUserId(userId)
  }
}
