package controllers.api

import com.google.inject._
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import service.user.definitions.MobileUserService
import scala.concurrent._
import ExecutionContext.Implicits.global

class SessionController @Inject()(
  mobileUserService: MobileUserService
) extends Controller {

  def updateSession = Action(parse.json) {implicit request =>    
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
    mobileUserService.createSession(content) match {
      case Success(session) => {
        val userId = (content \ "userId").as[String]
        mobileUserService.updateMobileUserSession(userId, session) match {
          case Success(s) => Ok
          case Failure(f) => BadRequest
        }
      }
      case Failure(f) => BadRequest
    }
  }
}

