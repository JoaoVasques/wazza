package dashboard

// dashboard proxy
import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import play.api.libs.concurrent.Akka._
import dashboard.messages._
import dashboard.workers._
import play.api.Play
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import persistence._
import application._
import user._

class DashboardProxy(
  system: ActorSystem,
  applicationProxy: ActorRef,
  userProxy: ActorRef
) extends Actor with Master[DashboardMessageRequest, DashboardWorker] {

  private val NUMBER_WORKERS = 5

  override def workersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(DashboardWorker.props(applicationProxy, userProxy))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def killRouter = {}

  protected def execute[DashboardMessageRequest](request: DashboardMessageRequest) = {
    workersRouter.route(request, sender())
  }

  def receive = masterReceive
}

object DashboardProxy {

  private var singleton: ActorRef = null

  def getInstance = {
    if(singleton == null) {
      singleton = Akka.system.actorOf(
        DashboardProxy.props(
          ActorSystem("dashboard"),
          ApplicationProxy.getInstance,
          UserProxy.getInstance
        ), name = "dashboard"
      )
    }
    singleton
  }

  def props(
    system: ActorSystem,
    applicationProxy: ActorRef,
    userProxy: ActorRef
  ): Props = Props(new DashboardProxy(system, applicationProxy, userProxy))
}
