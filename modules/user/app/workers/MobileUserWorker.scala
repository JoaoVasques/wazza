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
import scala.util.{Failure, Success}
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json._
import persistence.MongoFactory
import user.messages._
import models.user._
import persistence.messages._
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.mutable.Map
import models.user.{CompanyData}
import scala.collection.mutable.Stack

class MobileUserWorker(
  databaseProxy: ActorRef
) extends Actor with Worker[MobileUserMessageRequest] with ActorLogging {

  private def persistenceReceive: Receive = {
    case r: PRBooleanResponse => {
      if(!r.res) {
        localStorage.get(r.hash) match {
          case Some(or) => {
            val req = or.originalRequest.asInstanceOf[MUCreate]
            val user = new MobileUser(req.userId)
            val insertReq = new Insert(new Stack, "collection", user)
            databaseProxy ! insertReq
          }
          case _ => {
            //TODO error
          }
        }
      }
    }
  }

  private def mobileUserReceive: Receive = {
    case m: MUCreate => mobileUserExists(m)
  }

  def receive = mobileUserReceive orElse persistenceReceive

  private def mobileUserExists(msg: MUCreate) = {
    val hash = localStorage.store(self, msg)
    msg.sendersStack = msg.sendersStack.push(self)
    val collection = MobileUser.getCollection(msg.companyName, msg.applicationName)
    val request = new Exists(msg.sendersStack, collection, MobileUser.KeyId, msg.userId, true, hash)
    databaseProxy ! request
  }
}

object MobileUserWorker {

  def props(databaseProxy: ActorRef): Props = Props(new MobileUserWorker(databaseProxy))
}

