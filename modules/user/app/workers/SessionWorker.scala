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
  databaseProxy: ActorRef
) extends Actor with Worker[SessionMessageRequest] with ActorLogging {

  def receive = {
    case m: SRSave => saveSession(m)
  }

  private def saveSession(msg: SRSave) = {
    def saveSessionAux = {
      val collection = MobileSession.getCollection(msg.companyName, msg.applicationName)
      val model = Json.toJson(msg.session)
      val request = new Insert(new Stack(), collection, model)
      databaseProxy ! request
    }

    def addSessionToHashCollection = {
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

    saveSessionAux
    addSessionToHashCollection
  }
}

object SessionWorker {

  def props(databaseProxy: ActorRef): Props = Props(new SessionWorker(databaseProxy))
}

