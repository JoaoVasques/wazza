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
//import play.api.libs.json._
import com.uservoice._
import scala.collection.JavaConverters._

protected[plugin] class UserVoiceActorImpl(
  subdomain: String,
  apiKey: String,
  secretKey: String
) extends UserVoiceActor {

  println(s"CREDS..$subdomain | $apiKey | $secretKey")

  private val client = new Client(subdomain, apiKey, secretKey)

  def createTicket(timestamp: Date, error: Exception): Unit = {
    lazy val url = "/api/v1/tickets.json"
    val date = new SimpleDateFormat("dd-MM-yy:HH:mm").format(timestamp)
    val msg = s"Error: ${error.toString}"
    val args = Map[String, Object](
      "email" -> "joao@wazza.io",
      "ticket" -> (Map(
        "state" -> "open",
        "subject" -> s"500 error - ${date}",
        "message" -> msg
      ).asJava)
    )
    client.post(url, args.asJava)
  }
}

