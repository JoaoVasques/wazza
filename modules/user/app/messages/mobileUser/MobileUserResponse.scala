package user.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import play.api.libs.json._
import models.user._

trait MobileUserResponse[T] extends WazzaMessage {
  val res: T
}

//TODO
