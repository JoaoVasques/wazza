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

class UserWorker(
  databaseProxy: ActorRef
) extends Actor with Worker[UserMessageRequest] with ActorLogging {

  private def persistenceResponses: Receive = {
    case _ => println
  }

  private def userRequests: Receive = {
    case m: URInsert => insert(m)
    case m: URFind => find(m, sender)
    case m: URExists => exists(m, sender)
    case m: URDelete => delete(m)
    case m: URAddApplication => addApplication(m)
    case m: URGetApplications => getApplications(m, sender)
    case m: URAuthenticate => authenticate(m, sender)
  }

  def receive = userRequests orElse persistenceResponses

  private def insert(msg: URInsert) = {
    def insertUserAux = {
      val collection = User.getCollection
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

  private def addApplication(msg: URAddApplication) = {}

  private def getApplications(msg: URGetApplications, sender: ActorRef) = {}

  private def authenticate(msg: URAuthenticate, sender: ActorRef) = {}
}

object UserWorker {

  def props(databaseProxy: ActorRef): Props = Props(new UserWorker(databaseProxy))
}
