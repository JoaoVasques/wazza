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
import scala.concurrent.duration._

case class MessageRetry(ttl: Int, msg: MobileUserMessageRequest)

class MessageRetryPool {
  private val DefaultTTL = 2

  @volatile private var pool: List[MessageRetry] = List()

  def addEntry(entry: MobileUserMessageRequest, ttl: Int = DefaultTTL) =  {
    val msg = new MessageRetry(ttl, entry)
    pool = pool :+ msg
    msg
  }

  def removeEntry(entry: MobileUserMessageRequest) = pool = pool.filter{(m: MessageRetry) => !entry.equals(m.msg)}

  def updateEntry(entry: MobileUserMessageRequest): MessageRetry = {
    pool.find((m: MessageRetry) => entry.equals(m.msg)) match {
      case Some(e) => {
        removeEntry(e.msg)
        if((e.ttl -1) > 0) {
          addEntry(e.msg, e.ttl -1)  
        } else null
      }
      case None => null
    }
  }

  def exists(entry: MobileUserMessageRequest): Boolean = pool.contains(entry)

  def get(entry: MobileUserMessageRequest) = pool.find((m: MessageRetry) => entry.equals(m.msg))
}

class MobileUserWorker(
  databaseProxy: ActorRef
) extends Actor with Worker[MobileUserMessageRequest] with ActorLogging {
  import context._

  private var retryMessagesPool = new MessageRetryPool

  private def handleCreateRequest(req: MUCreate) = {
    val user = new MobileUser(req.userId, List[SessionResume](), List[PurchaseResume](), List[DeviceInfo]())
    val collection = MobileUser.getCollection(req.companyName, req.applicationName)
    val insertReq = new Insert(new Stack, collection, user)
    databaseProxy ! insertReq
  }

  private def handleAddSessionInfoRequest(req: MUAddSessionInfo) = {
    val sessionResume = new SessionResume(req.sessionId, req.sessionStart)
    val collection = MobileUser.getCollection(req.companyName, req.applicationName)
    val request = new AddElementToArray[JsValue](
      new Stack, collection, MobileUser.KeyId,
      req.userId, MobileUser.SessionsKey, MobileUser.readJsonSessionResume(sessionResume)
    )
    databaseProxy ! request
  }

  private def handleAddPurchaseIdRequest(req: MUAddPurchaseId) = {
    val purchaseResume = new PurchaseResume(req.purchaseId, req.purchaseDate)
    val collection = MobileUser.getCollection(req.companyName, req.applicationName)
    val request = new AddElementToArray[JsValue](
      new Stack, collection, MobileUser.KeyId,
      req.userId, MobileUser.PurchasesKey, MobileUser.readJsonPurchaseResume(purchaseResume)
    )
    databaseProxy ! request
  }

  private def persistenceReceive: Receive = {
    case r: PRBooleanResponse => {
      if(!r.res) {
        localStorage.get(r.hash) match {
          case Some(or) => {
            or.originalRequest match {
              case m: MUCreate => handleCreateRequest(m)
              case m: MUAddSessionInfo => {
                val msg = if(!retryMessagesPool.exists(m)) {
                  retryMessagesPool.addEntry(m)
                } else {
                  retryMessagesPool.get(m).get
                }
                system.scheduler.scheduleOnce(1000 millis, self, msg)
              }
            }
          }
          case _ => {
            //TODO error
          }
        }
      } else {
        localStorage.get(r.hash) match {
          case Some(or) => {
            or.originalRequest match {
              case m: MUAddSessionInfo => handleAddSessionInfoRequest(m)
              case m: MUAddPurchaseId => handleAddPurchaseIdRequest(m)
            }
          }
          case _ => {
            //TODO error
          }
        }
      }
    }
  }

  private def retryReceive: Receive = {
    case m: MessageRetry => {
      val updated = retryMessagesPool.updateEntry(m.msg)
      if(updated != null) {
        mobileUserExists[MUAddSessionInfo](updated.msg.asInstanceOf[MUAddSessionInfo])
      }
    }
  }

  private def mobileUserReceive: Receive = {
    case m: MUCreate => mobileUserExists[MUCreate](m)
    case m: MUAddSessionInfo => mobileUserExists[MUAddSessionInfo](m)
    case m: MUAddPurchaseId => mobileUserExists[MUAddPurchaseId](m)
  }

  def receive = mobileUserReceive orElse persistenceReceive orElse retryReceive

  private def mobileUserExists[T <: MobileUserMessageRequest](msg: T) = {
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

