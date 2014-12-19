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
import org.mindrot.jbcrypt.BCrypt
import common.actors._

class UserWorker(
  databaseProxy: ActorRef
) extends Actor with Worker[UserMessageRequest] with ActorLogging {

  def handleOptionResponse(m: PROptionResponse) {
    localStorage.get(m.hash) match {
      case Some(req) => {
        req.originalRequest match {
          case or: URFind => {
            val response = new UROptionResponse(req.originalRequest.sendersStack, m.res, req.originalRequest.hash)
            sendResults[UROptionResponse, URFind](
              req.originalRequest.asInstanceOf[URFind],
              req.sender,
              response
            )
          }
          case or: URGetApplications => {
            val applications = User.buildFromOption(m.res).get.applications
            val response = new URApplicationsResponse(
               req.originalRequest.sendersStack,
              applications,
              req.originalRequest.hash
            )
            sendResults[URApplicationsResponse, URGetApplications](
              req.originalRequest.asInstanceOf[URGetApplications],
              req.sender,
              response
            )
          }

          case or: URAuthenticate => {
            val optUser = User.buildFromOption(m.res).filter{user =>
              BCrypt.checkpw(or.password, user.password)
            }
            val response = new URAuthenticationResponse(
              req.originalRequest.sendersStack,
              optUser,
              req.originalRequest.hash
            )

            sendResults[URAuthenticationResponse, URAuthenticate](
              req.originalRequest.asInstanceOf[URAuthenticate],
              req.sender,
              response
            )
          }
        }
      }
      case None => {
        log.error("Cannot find request on local storage")
        //TODO send error message
      }
    }
  }

  private def handleBooleanResponse(m: PRBooleanResponse) = {
    localStorage.get(m.hash) match {
      case Some(req) => {
        req.originalRequest match {
          case or: URExists => {
            val response = new URBooleanResponse(
              or.sendersStack,
              m.res,
              or.hash
            )
            sendResults[URBooleanResponse, URExists](
              or.asInstanceOf[URExists],
              req.sender,
              response
            )
          }
          case or: URValidate => {
            val response = new URValidationResponse(
              or.sendersStack,
              (!m.res),
              or.hash
            )
            sendResults[URValidationResponse, URValidate](
              or.asInstanceOf[URValidate],
              req.sender,
              response
            )
          }
          case unkown => {
            log.error("Handle Boolean response: unkown response - " + unkown )
            //TODO send error message
          }
        }
      }
      case None => {
        log.error("Cannot find request on local storage")
        //TODO send error message
      }
    }    
  }

  private def handleInsertResponse(m: PRInsertResponse) = {
    localStorage.get(m.hash) match {
      case Some(req) => {
        val user = m.res.validate[User].fold(
          valid = {v => v}, invalid = {_ => null}
        )

        val response = new URUserResponse(
          req.originalRequest.sendersStack,
          user,
          req.originalRequest.hash
        )

        sendResults[URUserResponse, URInsert](
          req.originalRequest.asInstanceOf[URInsert],
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

  private def persistenceResponses: Receive = {
    case m: PROptionResponse => handleOptionResponse(m)
    case m: PRBooleanResponse => handleBooleanResponse(m)
    case m: PRInsertResponse => handleInsertResponse(m)
  }

  private def userRequests: Receive = {
    case m: URInsert => insert(m)
    case m: URFind => find(m, sender)
    case m: URExists => exists(m, sender)
    case m: URDelete => delete(m)
    case m: URAddApplication => addApplication(m)
    case m: URGetApplications => getApplications(m, sender)
    case m: URValidate => validate(m, sender)
    case m: URAuthenticate => authenticate(m, sender)
  }

  def receive = userRequests orElse persistenceResponses

  private def insert(msg: URInsert) = {
    def insertUserAux = {
      val hash = localStorage.store(sender, msg)
      val collection = User.getCollection
      msg.sendersStack = msg.sendersStack.push(self)
      val user = msg.user
      user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
      val request = new Insert(new Stack, collection, Json.toJson(user))
      databaseProxy ! request
    }

    def addUserToCompanyData = {
      val companyData = new CompanyData(msg.user.company, List[String]())
      val request = new Insert(new Stack, CompanyData.Collection, Json.toJson(companyData))
      databaseProxy ! request
    }

    insertUserAux
    addUserToCompanyData
  }

  private def find(msg: URFind, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = User.getCollection
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Get(msg.sendersStack, collection, User.Id, msg.email, null, false, hash)
    databaseProxy ! request
  }

  private def exists(msg: URExists, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = User.getCollection
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Exists(msg.sendersStack, collection, User.Id, msg.email, false, hash)
    databaseProxy ! request
  }

  private def delete(msg: URDelete) = {
    val collection = User.getCollection
    val request = new Delete(new Stack, collection, Json.toJson(msg.user))
    databaseProxy ! request
  }

  private def addApplication(msg: URAddApplication) = {
    val collection = User.getCollection
    val request = new AddElementToArray[String](
      new Stack,
      collection,
      User.Id,
      msg.email,
      User.ApplicationsField,
      msg.applicationId
    )
    databaseProxy ! request
  }

  private def getApplications(msg: URGetApplications, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = User.getCollection
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Get(msg.sendersStack, collection, User.Id, msg.email, null, false, hash)
    databaseProxy ! request
  }

  private def authenticate(msg: URAuthenticate, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = User.getCollection
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Get(msg.sendersStack, collection, User.Id, msg.email, null, false, hash)
    databaseProxy ! request
  }

  private def validate(msg: URValidate, sender: ActorRef) = {
    val hash = localStorage.store(sender, msg)
    val collection = User.getCollection
    msg.sendersStack = msg.sendersStack.push(self)
    val request = new Exists(msg.sendersStack, collection, User.Id, msg.email, false, hash)
    databaseProxy ! request
  }

  private def sendResults[R <: UserResponse[_], T <: UserMessageRequest](
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

object UserWorker {

  def props(databaseProxy: ActorRef): Props = Props(new UserWorker(databaseProxy))
}

