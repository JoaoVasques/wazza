package common.actors

import play.api.libs.concurrent.Akka._
import akka.actor.{Actor}
import common.messages._

trait Worker[S <: WazzaMessage] {
  this: Actor =>

  protected val localStorage = new InternalMessageStorage[S]

  def workerReceive: Receive = {
    case m => println(s"received " + m)
  }
}
