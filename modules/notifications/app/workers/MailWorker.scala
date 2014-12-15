package notifications.workers

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
import notifications.messages._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{ WSResponse, WS }
import play.api._

class MailWorker(
  apiKey: String,
  endpoint: String
) extends Actor with Worker[MailRequest] with ActorLogging {

  private def generateEndpoint(module: String, action: String) : String = {
		return endpoint + module + "/" + action + ".json"
	}

  private def sendEmail(subject: String, to: List[String], message: String): Unit = {
    val params = Json.obj(
      "key" -> apiKey,
      "message" -> Json.obj(
        "subject" -> subject,
        "text" -> message,
        "from_email" -> "no-reply@wazza.io",
        "from_name" -> "Wazza",
        "to" -> (to map {m => Json.obj("email" -> m, "type" -> "to")})
      )
    )
    WS.url(generateEndpoint("messages", "send")).post(params) map {res =>
      val status = (Json.parse(res.body) \ "status").as[String]
      if(status != "sent") {
        log.error("Email was not sent")
      }
    }
  }

  def receive = {
    case m: SendEmail => sendEmail(m.subject, m.to, m.message)
  }
}

object MailWorker {

  private case class MailCredentials(apiKey: String, endpoint: String)
  private object MailCredentials {
    def apply(ops: (Option[String], Option[String])): Option[MailCredentials] = {
      ops match {
        case _ if(ops._1.isDefined && ops._2.isDefined) => {
          Some(new MailCredentials(ops._1.get, ops._2.get))
        }
        case _ => None
      }
    }
  }

  private def parseConfig: Option[MailCredentials] = {
    def getConfigElement(config: Configuration, key: String): Option[String] = {
      try {
        Some(config.underlying.root.get(key).render.filter(_ != '"'))
      } catch {
        case _: Throwable => None
      }
    }

    Play.current.configuration.getConfig("mandrill") match {
      case Some(conf) => {
        MailCredentials(
          getConfigElement(conf, "apiKey"),
          getConfigElement(conf, "endpoint")
        )
      }
      case _ => None
    }
  }

  def apply: MailWorker = {
    parseConfig match {
      case Some(config) => new MailWorker(config.apiKey, config.endpoint)
      case _ => throw new Exception("Error occurred while initializing Mail worker")
    }
  }

  def props: Props = Props(MailWorker.apply)
}

