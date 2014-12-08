package application.workers

//TODO ApplicationWorker
import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import play.api.libs.concurrent.Akka._
import persistence.messages._
import java.text.SimpleDateFormat
import java.util.Date
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.util.Try
import play.api.libs.functional.syntax._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play
import play.api.Play.current
import com.mongodb.util.JSON
import scala.language.implicitConversions
import com.mongodb.casbah.Imports._
import scala.util.{Failure, Success}
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json._
import com.mongodb.casbah.Imports.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import org.joda.time.DateTime
import scala.collection.immutable.StringOps
import persistence.MongoFactory
import application.messages._
import models.application._
import persistence.messages._
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.mutable.Map
import WazzaApplicationImplicits._

class InternalMessageStorage {
  private val BASE = 32
  private val BITS = 130
  private val random = new SecureRandom
  private def generateId = new BigInteger(BITS, random).toString(32)

  case class StorageElement(originalRequest: ApplicationMessageRequest, sender: ActorRef)
  private val storage: Map[String, StorageElement] = Map()

  def store(sender: ActorRef, msg: ApplicationMessageRequest): String = {
    val id = generateId
    storage += (id -> new StorageElement(msg, sender))
    id
  }

  def get(id: String): Option[StorageElement] = {
    val s = storage.get(id)
    storage -= id
    s
  }
}

class ApplicationWorker(
  databaseProxy: ActorRef
) extends Actor with Worker with ActorLogging {

  private val localStorage = new InternalMessageStorage

  private def persistenceReceive: Receive = {
    case m: PROptionResponse => {
      localStorage.get(m.hash) match {
        case Some(req) => {
          val response = new AROptionResponse(req.originalRequest.sendersStack, m.res, req.originalRequest.hash)
          sendResults[AROptionResponse, ARFind](
            req.originalRequest.asInstanceOf[ARFind],
            req.sender ,
            response
          )
          //sendFindResult(req.originalRequest.asInstanceOf[ARFind], req.sender, m)
        }
        case None => {
          log.error("Cannot find request on local storage")
          //TODO send error message
        }
      }
    }
    case m: PRBooleanResponse => {
      localStorage.get(m.hash) match {
        case Some(req) => {
          val response = new ARBooleanResponse(req.originalRequest.sendersStack, m.res, req.originalRequest.hash)
          sendResults[ARBooleanResponse, ARExists](
            req.originalRequest.asInstanceOf[ARExists],
            req.sender ,
            response
          )
        }                                      
        case None =>{
          log.error("Cannot find request on local storage")
          //TODO send error message
        }
      }
      println("\n received boolean msg: " + m)
    }
  }

  private def applicationRequests: Receive = {
    case m: ARInsert => {}
    case m: ARDelete => {}
    case m: ARExists => {}
    case m: ARFind => find(m, sender)
  }

  def receive = applicationRequests orElse persistenceReceive

  private def sendResponse[R <: ApplicationMessageRequest](
    request: R,
    msg: ApplicationResponse[_],
    sender: ActorRef
  ) = {
    if(request.direct) {
      sender ! msg
    } else {
      msg.sendersStack.head ! msg
    }
  }

  private def insertApplication(msg: ARInsert, sender: ActorRef) = {

  }

  private def deleteApplication(msg: ARDelete, sender: ActorRef) = {

  }

  private def exists(msg: ARExists, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = WazzaApplication.getCollection(msg.companyName, msg.name)
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Exists(msg.sendersStack, collection, WazzaApplication.Key, msg.name, false, hash)

    databaseProxy ! request
  }

  private def find(msg: ARFind, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = WazzaApplication.getCollection(msg.companyName, msg.appName)
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Get(msg.sendersStack, collection, WazzaApplication.Key, msg.appName, null, false, hash)

    log.info("received find request: " + msg.toString)

    databaseProxy ! request
  }

  private def sendResults[R <: ApplicationResponse[_], T <: ApplicationMessageRequest](
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

object ApplicationWorker {

  def props(databaseProxy: ActorRef): Props = Props(new ApplicationWorker(databaseProxy))
}
