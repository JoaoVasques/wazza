package application

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import play.api.libs.concurrent.Akka._
import application.messages._
import application.workers._
import com.mongodb.casbah.Imports._
import play.api.Play
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current

class ApplicationProxy(
  system: ActorSystem
) extends Actor with Master[ApplicationMessageRequest, ApplicationWorker] {

  private val NUMBER_WORKERS = 5

  override def workersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(Props[ApplicationWorker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def killRouter = {}

  protected def execute[ApplicationMessageRequest](request: ApplicationMessageRequest) = {
    workersRouter.route(request, sender())
  }

  def receive = masterReceive
}

object ApplicationProxy {

  private var singleton: ActorRef = null

  def getInstance = {
    if(singleton == null) {
      singleton = Akka.system.actorOf(ApplicationProxy.props(ActorSystem("application")), name = "application")
    }
    singleton
  }

  def props(system: ActorSystem): Props = Props(new ApplicationProxy(system))
}
