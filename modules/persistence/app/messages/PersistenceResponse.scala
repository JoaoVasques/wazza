package persistence.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import play.api.libs.json._


trait PersistenceResponse[T] extends WazzaMessage {
  val res: T
}

case class PRInsertResponse(
  var sendersStack: Stack[ActorRef],
  res: JsValue,
  hash: String = null
) extends PersistenceResponse[JsValue]

case class PRBooleanResponse(
  var sendersStack: Stack[ActorRef],
  res: Boolean,
  hash: String = null
) extends PersistenceResponse[Boolean]

case class PROptionResponse(
  var sendersStack: Stack[ActorRef],
  res: Option[JsValue],
  hash: String = null
) extends PersistenceResponse[Option[JsValue]]

case class PRListResponse(
  var sendersStack: Stack[ActorRef],
  res: List[JsValue],
  hash: String = null
) extends PersistenceResponse[List[JsValue]]

case class PRJsArrayResponse(
  var sendersStack: Stack[ActorRef],
  res: JsArray,
  hash: String = null
) extends PersistenceResponse[JsArray]

