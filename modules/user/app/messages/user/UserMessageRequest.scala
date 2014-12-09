package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.user._

trait UserMessageRequest extends WazzaMessage {

  def direct: Boolean
}

case class URInsert(
  var sendersStack: Stack[ActorRef],
  user: User,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URFind(
  var sendersStack: Stack[ActorRef],
  email: String,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URExists(
  var sendersStack: Stack[ActorRef],
  email: String,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URDelete(
  var sendersStack: Stack[ActorRef],
  user: User,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URAddApplication(
  var sendersStack: Stack[ActorRef],
  email: String,
  applicationId: String,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URGetApplications(
  var sendersStack: Stack[ActorRef],
  email: String,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest

case class URAuthenticate(
  var sendersStack: Stack[ActorRef],
  email: String,
  password: String,
  direct: Boolean = false,
  hash: String = null
) extends UserMessageRequest







// TODO user message request
