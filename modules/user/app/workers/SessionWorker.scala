package user.workers

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import play.api.libs.concurrent.Akka._
import play.api.libs.json.JsArray
import play.api.libs.functional.syntax._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play
import play.api.Play.current
import scala.language.implicitConversions
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json._
import persistence.MongoFactory
import user.messages._
import models.user._
import persistence.messages._
import scala.collection.mutable.Stack

class SessionWorker(
  databaseProxy: ActorRef,
  userProxy: ActorRef
) extends Actor with Worker[SessionMessageRequest] with ActorLogging {

  private def saveSessionAux(msg: SRSave) = {
    val collection = MobileSession.getCollection(msg.companyName, msg.applicationName)
    val model = Json.toJson(msg.session)
    val request = new Insert(new Stack(), collection, model)
    databaseProxy ! request
  }

  private def addSessionToHashCollection(msg: SRSave) = {
    val collection = MobileSessionInfo.collection
    val model = Json.toJson(new MobileSessionInfo(
      msg.session.id,
      msg.session.userId,
      msg.applicationName,
      msg.companyName
    ))
    val request = new Insert(new Stack(), collection, model)
    databaseProxy ! request
  }

  private def addMobileUser(userId: String, companyName: String, applicationName: String) = {
    userProxy ! new MUCreate(new Stack, companyName, applicationName, userId)
  }

  private def persistenceReceive: Receive = {
    case r: PRBooleanResponse => {
      if(!r.res) {
        localStorage.get(r.hash) match {
          case Some(or) => {
            val req = or.originalRequest.asInstanceOf[SRSave]
            saveSessionAux(req)
            addSessionToHashCollection(req)
            addMobileUser(req.session.userId, req.companyName, req.applicationName)
          }
          case _ => {
            //TODO error
          }
        }
      } else {
        //TODO session exists - log error
      }
    }
  }

  private def sessionReceive: Receive = {
    case m: SRSave => sessionExists(m)
  }

  def receive = sessionReceive orElse persistenceReceive

  private def sessionExists(msg: SRSave) = {
    val hash = localStorage.store(self, msg)
    msg.sendersStack = msg.sendersStack.push(self)
    val collection = MobileSession.getCollection(msg.companyName, msg.applicationName)
    val request = new Exists(msg.sendersStack, collection, MobileSession.Id, msg.session.id, true, hash)
    databaseProxy ! request
  }
}

object SessionWorker {

  def props(
    databaseProxy: ActorRef,
    userProxy: ActorRef
  ): Props = Props(new SessionWorker(databaseProxy, userProxy))
}

