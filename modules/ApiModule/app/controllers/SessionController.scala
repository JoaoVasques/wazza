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
import service.application.definitions.{ApplicationService}
import org.joda.time.format.DateTimeFormat
import org.joda.time.Interval
import scala.concurrent._
import ExecutionContext.Implicits.global

class SessionController @Inject()(
  mobileUserService: MobileUserService,
  sessionService: MobileSessionService,
  applicationService: ApplicationService
) extends Controller {

  private def createNewSessionInfo(content: JsValue): Future[SimpleResult] = {
    def generateHash(content: String) = {
      val md = MessageDigest.getInstance("SHA-256")
      md.update(content.getBytes("UTF-8"))
      Hex.encodeHexString(md.digest())
    }

    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")
    val start = formatter.parseDateTime((content \ "startTime").as[String])
    val end = formatter.parseDateTime((content \ "endTime").as[String])

    val promise = Promise[SimpleResult]
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
        sessionService.insert(companyName, applicationName, session) map { r =>
          promise.success(Ok)
        } recover {
          case ex: Exception => {
            promise.failure(ex)
            null
          }
        }
      }
      case Failure(f) => promise.failure(new Exception(""))
    }
    promise.future
  }

  def saveSession(companyName: String, applicationName: String) = Action.async(parse.json) {implicit request =>
    val content = (Json.parse((request.body \ "content").as[String].replace("\\", "")) \ "session").as[JsArray]
    applicationService.exists(companyName, applicationName) flatMap {exists =>
      if(!exists){
        Future.successful(NotFound("Application"))
      } else {
        val futureResult = Future.sequence(content.value.map{(session: JsValue) => {
          val userId = (session  \ "userId").as[String]
          mobileUserService.exists(companyName, applicationName, userId) map {exist =>
            if(!exist) {
              mobileUserService.createMobileUser(companyName, applicationName, userId) map {r =>
                createNewSessionInfo(session)
              } recover {
                case ex: Exception => Future.failed(ex)
              }
            } else Future.failed(new Exception("duplicated session"))
          }
        }})

        futureResult map {
          _ => Ok
        } recover {
          case _ => InternalServerError("Save session error: session not saved")
        }
      }
    }
  }
}
