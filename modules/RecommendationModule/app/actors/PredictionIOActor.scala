package actors.recommendation

import akka.actor.{Props, ActorRef, Actor}
import io.prediction.CreateUserRequestBuilder
import models.user.MobileUser
import play.api.Logger
import play.api.Play
import play.api.libs.json.Json
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
import play.api.libs.ws.{ Response, WS }

class PredictionIOActor extends Actor {

  private case class Credentials(url: String, appKey: String)

  private lazy val predictionCredentials: Credentials = {
    Play.current.configuration.getConfig("prediction-io") match {
      case Some(config) => {
        val url = config.underlying.root.get("url").render.filter(_ != '"') + "/"
        val key = config.underlying.root.get("appKey").render.filter(_ != '"')
        new Credentials(url, key)
      }
      case _ => {
        Logger.error("No prediction-io credentials found")
        null
      }
    }
  }

  private def addUser(user: MobileUser) = {
    val url =  predictionCredentials.url + "users.json"
    val params = Json.obj(
      "pio_appkey" -> predictionCredentials.appKey,
      "pio_uid" -> user.userId,
      "osType" -> user.osType
    )

    WS.url(url).post(params)
  }

  def buildActorResponse(res: Response)  = {

    def isError(statusCode: Int): Boolean = {
      (statusCode >= 200 && statusCode < 300)
    }

    if(isError(res.status)) {
      ResponseMessage(Json.parse(res.body))
    } else {
      ErrorMessage(res.body)
    }
  }

  def receive = {
    case AddUserMessage(user) => {
      val sendTo = context.sender
      addUser(user).map{response =>
        sendTo ! buildActorResponse(response)
      }
    }
    case AddItemMessage(item) => {

    }
  }
}

object PredictionIOActor {
  lazy val get = Akka.system.actorOf(Props(classOf[PredictionIOActor]))
}

