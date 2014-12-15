package application.workers

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
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json._
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
import models.user.{CompanyData}
import scala.collection.mutable.Stack
import notifications._
import notifications.messages._

class ApplicationWorker(
  databaseProxy: ActorRef,
  notificationProxy: ActorRef
) extends Actor with Worker[ApplicationMessageRequest] with ActorLogging {

  private def persistenceReceive: Receive = {
    case m: PROptionResponse => {
      localStorage.get(m.hash) match {
        case Some(req) => {
          val response = new AROptionResponse(req.originalRequest.sendersStack, m.res, req.originalRequest.hash)
          sendResults[AROptionResponse, ARFind](
            req.originalRequest.asInstanceOf[ARFind],
            req.sender,
            response
          )
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
            req.sender,
            response
          )
        }                                      
        case None =>{
          log.error("Cannot find request on local storage")
          //TODO send error message
        }
      }
    }
    case m: PRInsertResponse => {
      localStorage.get(m.hash) match {
        case Some(req) => {
          handleInsertApplicationResult(m, req.originalRequest.asInstanceOf[ARInsert])
        }
        case None => {
          log.error("Cannot find request on local storage")
          //TODO send error message
        }
      }
    }
  }

  private def applicationRequests: Receive = {
    case m: ARInsert => insert(m, sender)
    case m: ARDelete => delete(m, sender)
    case m: ARExists => exists(m, sender)
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

  private def insert(msg: ARInsert, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = WazzaApplication.getCollection(msg.companyName, msg.application.name)
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Insert(msg.sendersStack, collection, msg.application, null, false, hash)
    databaseProxy ! request

    val email = s"Company $msg.companyName has created a new application ${msg.application.name} for ${msg.application.appType}"
    val mailRequest = new SendEmail(new Stack, List("support@wazza.io"), "New Application Created", email)
    notificationProxy ! mailRequest
  }

  private def delete(msg: ARDelete, sender: ActorRef) = {
    val collection = WazzaApplication.getCollection(msg.companyName, msg.application.name)
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Delete(msg.sendersStack, collection, msg.application, false, null)
    databaseProxy ! request
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
    databaseProxy ! request
  }

  private def handleInsertApplicationResult(response: PRInsertResponse, request: ARInsert) = {


    val application = WazzaApplicationImplicits.buildFromJson(response.res)
    def addApplication = {
      val addApplicationRequest = new AddElementToArray[String](
        request.sendersStack,
        CompanyData.Collection,
        CompanyData.Key,
        request.companyName,
        CompanyData.Apps,
        application.name,
        true
      )

      databaseProxy ! addApplicationRequest
    }
    def saveAppData() = {
      val model = Json.obj(
        "token" -> application.credentials.sdkToken,
        "companyName" -> request.companyName,
        "applicationName" -> application.name
      )
      val collection = "RedirectionTable"
      val saveAppDataRequest = new Insert(
        new Stack[ActorRef](),
        collection,
        model
      )
      databaseProxy ! saveAppDataRequest
    }

    addApplication
    saveAppData
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

  def props(
    databaseProxy: ActorRef,
    notificationProxy: ActorRef
  ): Props = Props(new ApplicationWorker(databaseProxy, notificationProxy))
}

