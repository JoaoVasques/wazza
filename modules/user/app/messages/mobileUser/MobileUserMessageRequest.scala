package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.user._

trait MobileUserMessageRequest extends WazzaMessage {
  def direct: Boolean
}

case class MUCreate(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  applicationName: String,
  userId: String,
  direct: Boolean = false,
  hash: String = null
) extends MobileUserMessageRequest

