package controllers.api

import com.google.inject._
import java.security.MessageDigest
import org.apache.commons.codec.binary.Hex
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import service.user.definitions.MobileUserService
import service.user.definitions.MobileSessionService
import scala.concurrent._
import ExecutionContext.Implicits.global

class SessionController @Inject()(
  mobileUserService: MobileUserService,
  sessionService: MobileSessionService
) extends Controller {

  private lazy val NewSession = 1
  private lazy val UpdateSession = 2

  private def getSessionRequestType(contentStr: String) = {
    val json = Json.toJson(contentStr)
    println("session content!")
    println(contentStr)
    (json \ "hash").asOpt[String] match {
      case Some(_) => UpdateSession
      case None => NewSession
    }
  }

  private def createNewSessionInfo(content: String) = {
    def generateHash(content: String) = {
      val md = MessageDigest.getInstance("SHA-256")
      md.update(content.getBytes("UTF-8"))
      Hex.encodeHexString(md.digest())
    }

    val jsonContent = Json.parse(content)
    sessionService.create(Json.obj(
      "id" -> generateHash(content),
      "userId" -> (jsonContent \ "userId").as[String],
      "sessionLength" -> 10, //default
      "startTime" -> "t", //TODO
      "deviceInfo" -> (jsonContent \ "deviceInfo"),
      "purchases" -> List[String]()
    )) match {
      case Success(session) => {
        val companyName = (jsonContent \ "companyName").as[String]
        val applicationName = (jsonContent \ "applicationName").as[String]
        sessionService.insert(companyName, applicationName, session) match {
          case Success(_) => Ok
          case Failure(_) => BadRequest
        }
      }
      case Failure(f) => {
        println(s"$f.getMessage")
        BadRequest
      }
    }
  }

  private def updateOldSessionInfo(content: String) = {
    Ok
  }

  def updateSession(companyName: String, applicationName: String) = Action(parse.json) {implicit request =>

    val content = (request.body \ "content").as[String].replace("\\", "")
    getSessionRequestType(content) match {
      case NewSession => createNewSessionInfo(content)
      case UpdateSession => updateOldSessionInfo(content)
    }
  }
}

