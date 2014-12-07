package common.messages

import akka.actor.{ActorRef}
import scala.collection.mutable.Stack

trait WazzaMessage {

  val sendersStack: Stack[ActorRef]

}
