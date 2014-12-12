package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack 
import models.user._

trait SessionMessageRequest extends WazzaMessage {
  def direct: Boolean
}

case class SRSave(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  applicationName: String,
  session: MobileSession,
  direct: Boolean = false,
  hash: String = null
) extends SessionMessageRequest

