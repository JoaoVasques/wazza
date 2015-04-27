package application.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import org.bson.types.ObjectId
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.application._

trait ApplicationMessageRequest extends WazzaMessage {
  def direct: Boolean
}

case class ARInsert(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  application: WazzaApplication,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

case class ARAddPayPalCredentials(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  applicationName: String,
  paypalCredentials: PayPalCredentials,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

case class ARAddPaymentSystem(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  applicationName: String,
  paymentSystem: Int,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

case class ARDelete(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  application: WazzaApplication,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

case class ARExists(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  name: String,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

case class ARFind(
  var sendersStack: Stack[ActorRef],
  companyName: String,
  appName: String,
  direct: Boolean = false,
  hash: String = null
) extends ApplicationMessageRequest

