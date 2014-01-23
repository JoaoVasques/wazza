package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import com.google.inject._
import service.user.definitions.{UserService}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.libs.json._
import models.user._
import play.api.libs.iteratee._
import scala.util.{Success, Failure}
import controllers.security._
import service.security.definitions.{TokenManagerService}

class LoginLogoutController @Inject()(
  userService: UserService,
  tokenService: TokenManagerService
) extends Controller with Security{

  val loginForm = Form {
    mapping(
      "email" -> email,
      "password" -> text
    )(userService.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def login = Action {
    Ok(views.html.login())
  }

  def logout = Action{implicit request =>
    request.headers.get(AuthTokenHeader) map { token =>
      Ok(routes.Application.index().url).discardingToken(token)(tokenService.remove)
    } getOrElse BadRequest(Json.obj("err" -> "No Token"))
  }

  def authenticate = Action.async(parse.json){implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future {
          BadRequest(Json.obj("errors" -> formWithErrors.errors.head.message))
      },
      user => {
        val token = tokenService.startNewSession(user.get.email)
        Future {
          Ok(Json.obj(
            "authToken" -> token,
            "userId" -> user.get.email,
            "url" -> "/home"// TODO routes.Application.test().url
          )).withToken(token)
        }
      }
    )
  }
}
