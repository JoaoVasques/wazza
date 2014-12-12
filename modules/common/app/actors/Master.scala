package common.actors

import common.messages._
import play.api.libs.concurrent.Akka._
import akka.actor.{Actor, Props}
import reflect.runtime.universe._
import akka.routing.Router
import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag}

trait Master[M <: WazzaMessage, W <: Worker[_]] {
  this:  Actor =>

  protected def killRouter

  protected def workersRouter: Router

  protected def execute[M](request: M)

  def masterReceive(implicit tag: ClassTag[M]): Receive = {
    case msg if tag.runtimeClass.isInstance(msg) => execute(msg.asInstanceOf[M])
  }
}

