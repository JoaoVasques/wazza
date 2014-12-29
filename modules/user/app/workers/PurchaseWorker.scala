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

class PurchaseWorker(
  databaseProxy: ActorRef
) extends Actor with Worker[PurchaseMessageRequest] with ActorLogging {

  def storePurchase(msg: PRSave) = {
    val collection = PurchaseInfo.getCollection(msg.companyName, msg.applicationName)
    val request = new Insert(msg.sendersStack, collection, msg.info)
    databaseProxy ! request
  }

  def saveUserAsBuyer(msg: PRSave) = {
    val collection = Buyer.getCollection(msg.companyName, msg.applicationName)
    val model = Json.obj("userId" -> msg.info.userId)
    val request = new Insert(msg.sendersStack, collection, model)
    databaseProxy ! request
  }


  private def persistenceReceive: Receive = {
    case r: PRBooleanResponse => {
      if(!r.res) {
        localStorage.get(r.hash) match {
          case Some(or) => {
            val req = or.originalRequest.asInstanceOf[PRSave]
            val purchase = req.info
            val collection = PurchaseInfo.getCollection(req.companyName, req.applicationName)
            val insertReq = new Insert(new Stack, collection, purchase)
            databaseProxy ! insertReq
          }
          case _ => {
            //TODO show error
          }
        }
      } else {
        //TODO show error on log
      }
    }
  }

  private def purchasesReceive: Receive = {
    case m: PRSave => purchaseExists(m)
  }

  def receive = purchasesReceive orElse persistenceReceive

  private def purchaseExists(msg: PRSave) = {
    val hash = localStorage.store(self, msg)
    msg.sendersStack = msg.sendersStack.push(self)
    val collection = PurchaseInfo.getCollection(msg.companyName, msg.applicationName)
    val request = new Exists(msg.sendersStack, collection, PurchaseInfo.Id, msg.info.id, true, hash)
    databaseProxy ! request
  }
}

object PurchaseWorker {

  def props(databaseProxy: ActorRef): Props = Props(new PurchaseWorker(databaseProxy))
}
