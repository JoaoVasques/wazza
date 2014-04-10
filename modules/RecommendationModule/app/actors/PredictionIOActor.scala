package actors.recommendation

import akka.actor.{Props, ActorRef, Actor}
import io.prediction.CreateUserRequestBuilder
import models.user.MobileSession
import models.user.MobileUser
import play.api.Logger
import play.api.Play
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
import play.api.libs.ws.{ Response, WS }
import service.definitions.recommendation.{RecommendationTypes}

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

  private def getUser(id: String) = {
    val url =  predictionCredentials.url + "users/" + id + ".json"
    WS.url(url).withQueryString("pio_appkey" -> predictionCredentials.appKey).get
  }

  private def addSession(session: MobileSession) = {
    def addUserSessionRelation(userId: String, sessionId: String) = {
      val url = predictionCredentials.url + "actions/u2i.json"
      val params = Json.obj(
        "pio_appkey" -> predictionCredentials.appKey,
        "pio_uid" -> userId,
        "pio_iid" -> sessionId,
        "pio_action" -> "view"
      )
      WS.url(url).post(params)
    }

    val url = predictionCredentials.url + "items.json"
    val params = Json.obj(
      "pio_appkey" -> predictionCredentials.appKey,
      "pio_iid" -> ("userId" + session.startTime),
      "pio_itypes" -> RecommendationTypes.SessionType,
      "userId" -> "",
      "length" -> session.length,
      "startTime" -> session.startTime,
      "osType" -> session.deviceInfo.osType,
      "name" -> session.deviceInfo.name,
      "version" -> session.deviceInfo.version,
      "model" -> session.deviceInfo.model
    )
    val sessionFuture = WS.url(url).post(params)

    sessionFuture flatMap {sessionRes =>
      if(isNotError(sessionRes.status)) {
        addUserSessionRelation("userId", ("userId" + session.startTime))
      } else {
        Future {sessionRes}
      }
    }
  }

  def isNotError(statusCode: Int): Boolean = {
    (statusCode >= 200 && statusCode < 300)
  }

  def buildActorResponse(res: Response)  = {
    if(isNotError(res.status)) {
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
    case GetUserMessage(id) => {
      val sendTo = context.sender
      getUser(id).map{response =>
        sendTo ! buildActorResponse(response)
      }
    }
    case AddSessionMessage(session) => {
      val sendTo = context.sender
      addSession(session).map{response =>
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

