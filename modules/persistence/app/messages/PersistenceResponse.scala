package persistence.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import play.api.libs.json._


trait PersistenceResponse[T] extends WazzaMessage {
  val res: T
}

case class PRBooleanResponse(
  sendersStack: Stack[ActorRef],
  res: Boolean
) extends PersistenceResponse[Boolean]

case class PROptionResponse(
  sendersStack: Stack[ActorRef],
  res: Option[JsValue]
) extends PersistenceResponse[Option[JsValue]]

case class PRListResponse(
  sendersStack: Stack[ActorRef],
  res: List[JsValue]
) extends PersistenceResponse[List[JsValue]]

case class PRJsArrayResponse(
  sendersStack: Stack[ActorRef],
  res: JsArray
) extends PersistenceResponse[JsArray]

