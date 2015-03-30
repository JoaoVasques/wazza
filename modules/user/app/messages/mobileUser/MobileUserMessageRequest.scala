package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.user._
import models.common._

abstract class MobileUserMessageRequest extends WazzaMessage {
  val direct: Boolean 
  val companyName: String
  val applicationName: String
  val userId: String
} 

case class MUCreate(
  direct: Boolean,
  companyName: String,
  applicationName: String,
  userId: String,
  deviceInfo: DeviceInfo,
  var sendersStack: Stack[ActorRef],
  hash: String = null
) extends MobileUserMessageRequest
 
case class MUAddSessionInfo(
  direct: Boolean,
  companyName: String,
  applicationName: String,
  userId: String,
  var sendersStack: Stack[ActorRef],
  sessionId: String,
  sessionStart: Date,
  platform: String,
  hash: String = null
) extends MobileUserMessageRequest

case class MUAddPurchaseId(
  direct: Boolean,
  companyName: String,
  applicationName: String,
  userId: String,
  var sendersStack: Stack[ActorRef],
  purchaseId: String,
  purchaseDate: Date,
  platform: String,
  hash: String = null
) extends MobileUserMessageRequest

