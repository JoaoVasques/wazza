package common.messages

import akka.actor.{ActorRef}
import scala.collection.mutable.Stack

trait WazzaMessage {

  var sendersStack: Stack[ActorRef]
  val hash: String

}
