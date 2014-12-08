package application.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import play.api.libs.json._
import models.application._

trait ApplicationResponse[T] extends WazzaMessage {
  val res: T
}

case class ARWApplicationResponse(
  var sendersStack: Stack[ActorRef],
  res: WazzaApplication,
  hash: String = null
) extends ApplicationResponse[WazzaApplication]

case class ARBooleanResponse(
  var sendersStack: Stack[ActorRef],
  res: Boolean,
  hash: String = null
) extends ApplicationResponse[Boolean]

case class AROptionResponse(
  var sendersStack: Stack[ActorRef],
  res: Option[WazzaApplication],
  hash: String = null
) extends ApplicationResponse[Option[WazzaApplication]]

