package notifications.plugin.actors

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Date
import play.api.libs.functional.syntax._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play
import play.api.Play.current
import scala.language.implicitConversions
import play.api.Logger
import scala.concurrent.Future
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{ WSResponse, WS }
import play.api.libs.json._

protected[plugin] class MandrillActor(
  apiKey: String,
  endpoint: String
) extends MailActor {

  private def generateEndpoint(module: String, action: String) : String = {
		return endpoint + module + "/" + action + ".json"
	}

  def sendEmail(subject: String, to: List[String], message: String): Unit = {
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
    
    WS.url(generateEndpoint("messages", "send")).post(params)
  }
}

