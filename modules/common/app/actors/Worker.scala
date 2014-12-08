package common.actors

import play.api.libs.concurrent.Akka._
import akka.actor.{Actor}
import common.messages._
// Worker

trait Worker {
  this: Actor =>

  def workerReceive: Receive = {
    case m => println(s"received " + m)
  }
}
