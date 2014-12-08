package persistence

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import play.api.libs.concurrent.Akka._
import persistence.messages._
import persistence.worker._
import com.mongodb.casbah.Imports._
import play.api.Play
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current

class PersistenceProxy (
  system: ActorSystem
) extends Actor with Master[PersistenceMessage, PersistenceWorker] {

  private val NUMBER_WORKERS = 5

  override def workersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(Props[PersistenceWorker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def killRouter = {

  }

  override def preStart() = {
    super.preStart()
  }

  override def postStop() = {
    MongoFactory.destroy
  }

  protected def execute[PersistenceMessage](request: PersistenceMessage) = {
    workersRouter.route(request, sender())
  }

  def receive = masterReceive
}

protected[persistence] object MongoFactory {

  private def getMongoURI() = {
    Play.current.configuration.getString("mongodb.uri") match {
      case Some(str) => {
        Some(MongoClientURI(str))
      }
      case _ => None
    }
  }

  private def getMongoClient() = {
    getMongoURI match {
      case Some(uri) => MongoClient(uri)
      case _ => throw new Exception("")
    }
  }

  private var _client: MongoClient = null

  private def client: MongoClient = {
    if(_client == null) {
      _client = getMongoClient
    }
    _client
  }

  def getCollection(collectionName: String) =  {
    val db = getMongoURI.get.database.get
    client(db)(collectionName)
  }

  def destroy() = {
    Logger.info("Destroying mongodb connection")
    client.close
  }
}

object PersistenceProxy {

  private var singleton: ActorRef = null

  def getConnector = {
    if(singleton == null){
      singleton = Akka.system.actorOf(PersistenceProxy.props(ActorSystem("Persistence")), name = "persistence")
    }
    singleton
  }

  def props(system: ActorSystem): Props = Props(new PersistenceProxy(system))
}
