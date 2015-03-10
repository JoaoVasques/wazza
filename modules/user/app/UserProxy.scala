package user

//TODO user proxy

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import play.api.libs.concurrent.Akka._
import user.messages._
import user.workers._
import com.mongodb.casbah.Imports._
import play.api.Play
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import persistence._

class UserProxy (
  system: ActorSystem,
  databaseProxy: ActorRef
) extends Actor with Master[WazzaMessage, UserWorker] {

  private val NUMBER_WORKERS = 5
  private val ROUTING_LOGIC = RoundRobinRoutingLogic()

  override def workersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(UserWorker.props(databaseProxy))
      context watch r
      ActorRefRoutee(r)
    }
    Router(ROUTING_LOGIC, routees)
  }

  private def purchasesWorkersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(PurchaseWorker.props(databaseProxy, self))
      context watch r
      ActorRefRoutee(r)
    }
    Router(ROUTING_LOGIC, routees)
  }

  private def sessionsWorkerRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(SessionWorker.props(databaseProxy, self))
      context watch r
      ActorRefRoutee(r)
    }
    Router(ROUTING_LOGIC, routees)
  }

  private def mobileUsersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(MobileUserWorker.props(databaseProxy))
      context watch r
      ActorRefRoutee(r)
    }
    Router(ROUTING_LOGIC, routees)
  }

  override def killRouter = {}

  protected def execute[WazzaMessage](request: WazzaMessage) = {
    request match {
      case r: UserMessageRequest =>  workersRouter.route(r, sender())
      case r: PurchaseMessageRequest => purchasesWorkersRouter.route(r, sender())
      case r: SessionMessageRequest => sessionsWorkerRouter.route(r, sender())
      case r: MobileUserMessageRequest => mobileUsersRouter.route(r, sender())
    }
  }

  def receive = masterReceive
}

object UserProxy  {

  private var singleton: ActorRef = null

  def getInstance(system: ActorSystem = Akka.system) = {
    if(singleton == null) {
      singleton = system.actorOf(
        UserProxy.props(ActorSystem("user"), PersistenceProxy.getInstance(system)), name = "user"
      )
    }
    singleton
  }

  def props(
    system: ActorSystem,
    databaseProxy: ActorRef
  ): Props = Props(new UserProxy(system, databaseProxy))
}
