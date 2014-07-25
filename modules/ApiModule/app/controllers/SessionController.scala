package controllers.api

import com.google.inject._
import java.security.MessageDigest
import models.user.MobileSession
import org.apache.commons.codec.binary.Hex
import org.joda.time.Seconds
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import service.user.definitions.MobileUserService
import service.user.definitions.MobileSessionService
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval

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

  private def createNewSessionInfo(content: JsValue) = {
    def generateHash(content: String) = {
      val md = MessageDigest.getInstance("SHA-256")
      md.update(content.getBytes("UTF-8"))
      Hex.encodeHexString(md.digest())
    }

    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")
    val start = formatter.parseDateTime((content \ "startTime").as[String])
    val end = formatter.parseDateTime((content \ "endTime").as[String])

    sessionService.create(Json.obj(
      "id" -> generateHash(content.toString),
      "userId" -> (content \ "userId").as[String],
      "sessionLength" -> (new Interval(start, end).toDurationMillis() / 1000),
      "startTime" -> (content \ "startTime").as[String],
      "deviceInfo" -> (content \ "deviceInfo"),
      "purchases" -> List[String]()
    )) match {
      case Success(session) => {
        val companyName = (content \ "companyName").as[String]
        val applicationName = (content \ "applicationName").as[String]
        sessionService.insert(companyName, applicationName, session) match {
          case Success(_) => Ok
          case Failure(_) => BadRequest
        }
      }
      case Failure(f) => BadRequest
    }
  }

  def saveSession(companyName: String, applicationName: String) = Action(parse.json) {implicit request =>
    val content = (Json.parse((request.body \ "content").as[String].replace("\\", "")) \ "session").as[JsValue]
    val userId = (content  \ "userId").as[String]

    if(!mobileUserService.exists(companyName, applicationName, userId)) {
      mobileUserService.createMobileUser(companyName, applicationName, userId)
    }

    createNewSessionInfo(content)
  }
}
