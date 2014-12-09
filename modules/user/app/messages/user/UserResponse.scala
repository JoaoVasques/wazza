package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import play.api.libs.json._
import models.user._

trait UserResponse[T] extends WazzaMessage {
  val res: T
}

case class URUserResponse(
  var sendersStack: Stack[ActorRef],
  res: User,
  hash: String = null
) extends UserResponse[User]

case class UROptionResponse(
  var sendersStack: Stack[ActorRef],
  res: Option[User],
  hash: String = null
) extends UserResponse[Option[User]]

case class URApplicationsResponse(
  var sendersStack: Stack[ActorRef],
  res: List[String],
  hash: String = null
) extends UserResponse[List[String]]


case class URBooleanResponse(
  var sendersStack: Stack[ActorRef],
  res: Boolean,
  hash: String = null
) extends UserResponse[Boolean]

case class URAuthenticationResponse(
  var sendersStack: Stack[ActorRef],
  res: Option[User],
  hash: String = null
) extends UserResponse[Option[User]]

