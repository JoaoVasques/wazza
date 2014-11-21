package controllers.api

import com.google.inject._
import models.user.MobileSession
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
import persistence.utils._
import controllers.security._

class SessionController @Inject()(
  mobileUserService: MobileUserService,
  sessionService: MobileSessionService,
  applicationService: ApplicationService
) extends Controller {

  private def createNewSessionInfo(content: JsValue): Future[SimpleResult] = {
    val start = DateUtils.buildJodaDateFromString((content \ "startTime").as[String])
    val end = DateUtils.buildJodaDateFromString((content \ "endTime").as[String])

    val promise = Promise[SimpleResult]
    sessionService.create(Json.obj(
      "id" -> (content \ "hash").as[String],
      "userId" -> (content \ "userId").as[String],
      "sessionLength" -> (new Interval(start, end).toDurationMillis() / 1000),
      "startTime" -> (content \ "startTime").as[String],
      "deviceInfo" -> (content \ "deviceInfo"),
      "purchases" -> (content \ "purchases").as[List[String]]
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

  def saveSession() = ApiSecurityAction.async(parse.json) {implicit request =>
    val companyName = request.companyName
    val applicationName = request.applicationName
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
            } else {
              createNewSessionInfo(session)
            }
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
