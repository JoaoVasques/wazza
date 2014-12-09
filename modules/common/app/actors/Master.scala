package common.actors

import common.messages._
import play.api.libs.concurrent.Akka._
import akka.actor.{Actor, Props}
import reflect.runtime.universe._
import akka.routing.Router
// Master

trait Master[M <: WazzaMessage, W <: Worker[_]] {
  this:  Actor =>

  override def preStart() = {
  }

  override def postStop() = {
    //TODO send poison messages to workers
  }

  protected def killRouter

  protected def workersRouter: Router

  protected def execute[M](request: M)

  def masterReceive: Receive = {
    case msg: M => execute(msg)
    //case _: PoisonPill
  }
}

