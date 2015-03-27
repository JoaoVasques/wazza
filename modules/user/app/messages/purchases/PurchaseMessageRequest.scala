package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.payments._

trait PurchaseMessageRequest extends WazzaMessage {
  def direct: Boolean
}

case class PRSave(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  applicationName: String,
  info: PurchaseInfo,
  direct: Boolean = false,
  hash: String = null
) extends PurchaseMessageRequest

