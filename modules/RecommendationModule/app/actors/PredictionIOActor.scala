package actors.recommendation

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka

class PredictionIOActor extends Actor {

  def receive = {
    case AddUserMessage(user) => {
      println("actor received user - $user")
      sender ! "OK" 
    }
    case AddItemMessage(item) => {

    }
  }
}

object PredictionIOActor {
  lazy val get = Akka.system.actorOf(Props(classOf[PredictionIOActor]), "predictionActor")
}

