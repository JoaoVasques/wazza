package notifications.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack

trait MailRequest extends WazzaMessage {
  
}

case class SendEmail(
  var sendersStack: Stack[ActorRef],
  to: List[String],
  subject: String,
  message: String,
  hash: String = null
) extends MailRequest
