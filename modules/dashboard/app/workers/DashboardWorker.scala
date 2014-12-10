package dashboard.workers

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import play.api.libs.concurrent.Akka._
import play.api.libs.functional.syntax._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play
import play.api.Play.current
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json._
import models.application._
import user.messages._
import application.messages._
import dashboard.messages._
import scala.collection.mutable.Map
import WazzaApplicationImplicits._
import scala.collection.mutable.Stack
import application._
import user._

class DashboardWorker(
  applicationProxy: ActorRef,
  userProxy: ActorRef
) extends Actor with Worker[DashboardMessageRequest] with ActorLogging {

  private def userResponses: Receive = {
    case _ => println
  }


  private def applicationResponses: Receive = {
    case m: ARFind => println
  }

  private def dashboardRequests: Receive = {
    case m: DROverviewBootstrap => overviewBootstrap(m, sender)
    case m: DRSettingsBootstrap => settingsBootstrap(m, sender)
  }

  def receive = dashboardRequests orElse applicationResponses orElse userResponses

  private def overviewBootstrap(msg: DROverviewBootstrap, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    msg.sendersStack = msg.sendersStack.push(self)
    val applicationsRequest = new URGetApplications(msg.sendersStack, msg.userId, false, hash)
    val userRequest = new URFind(msg.sendersStack, msg.userId, false, hash)
    userProxy ! applicationsRequest
    userProxy ! userRequest
  }

  private def settingsBootstrap(msg: DRSettingsBootstrap, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    msg.sendersStack = msg.sendersStack.push(self)
    val applicationsRequest = new URGetApplications(msg.sendersStack, msg.userId, false, hash)
    val userRequest = new URFind(msg.sendersStack, msg.userId, false, hash)
    userProxy ! applicationsRequest
    userProxy ! userRequest
  }


  private def sendResults[R <: DashboardResponse[_], T <: DashboardMessageRequest](
    msg: T,
    sender: ActorRef,
    response: R
  ) = {
    if(msg.sendersStack.isEmpty || msg.direct) {
      sender ! response
    } else {
      msg.sendersStack.pop ! response
    }
  }
}

object DashboardWorker {

  def props(
    applicationProxy: ActorRef,
    userProxy: ActorRef
  ): Props = Props(new DashboardWorker(applicationProxy, userProxy))
}


