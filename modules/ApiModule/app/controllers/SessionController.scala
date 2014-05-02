package controllers.api

import com.google.inject._
import java.security.MessageDigest
import models.user.MobileSession
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
    (json \ "type").as[Int] match {
      case NewSession => UpdateSession
      case UpdateSession => NewSession
    }
  }

  private def createMobileUser(companyName: String, applicationName: String, userId: String) = {
    if(!mobileUserService.exists(companyName, applicationName, userId)) {
      mobileUserService.createMobileUser(companyName, applicationName, userId)
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
      "sessionLength" -> 0,
      "startTime" -> (jsonContent \ "startTime").as[String],
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
        BadRequest
      }
    }
  }

  def newSession(companyName: String, applicationName: String) = Action(parse.json) {implicit request =>
    val content = (request.body \ "content").as[String].replace("\\", "")
    val userId = ((Json.parse(content)) \ "userId").as[String]
    createMobileUser(companyName, applicationName, userId)
    createNewSessionInfo(content)
  }

  def endSession(companyName: String, applicationName: String) = Action(parse.json) {implicit request =>
    val content = (request.body \ "content").as[String].replace("\\", "")
    val json = Json.parse(content)
    val hash = (json \ "hash").as[String]

    sessionService.get(hash) match {
      case Some(session) => {
        println(s"$session")
        sessionService.calculateSessionLength(session, (json \ "date").as[String])
        Ok
      }
      case None => BadRequest("session does not exist")
    }
  }
}
