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
  sendersStack: Stack[ActorRef],
  res: WazzaApplication
) extends ApplicationResponse[WazzaApplication]

case class ARBooleanResponse(
  sendersStack: Stack[ActorRef],
  res: Boolean
) extends ApplicationResponse[Boolean]

case class AROptionResponse(
  sendersStack: Stack[ActorRef],
  res: Option[WazzaApplication]
) extends ApplicationResponse[Option[WazzaApplication]]

