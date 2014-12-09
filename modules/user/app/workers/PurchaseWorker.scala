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

  def receive = {
    case m: PRSave => savePurchase(m)
  }

  private def savePurchase(msg: PRSave) = {
    def storePurchase = {
      val collection = PurchaseInfo.getCollection(msg.companyName, msg.applicationName)
      val request = new Insert(msg.sendersStack, collection, Json.toJson(msg.info))
      databaseProxy ! request
    }

    def saveUserAsBuyer = {
      val collection = Buyer.getCollection(msg.companyName, msg.applicationName)
      val model = Json.obj("userId" -> msg.info.userId)
      val request = new Insert(msg.sendersStack, collection, model)
      databaseProxy ! request
    }

    storePurchase
    saveUserAsBuyer
  }
}

object PurchaseWorker {

  def props(databaseProxy: ActorRef): Props = Props(new PurchaseWorker(databaseProxy))
}
