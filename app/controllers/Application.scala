package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.security.{AuthConfigImpl}
import jp.t2v.lab.play2.auth.LoginLogout
import com.google.inject._
import service.user.definitions.{UserService}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.libs.json._

class Application @Inject()(
  userService: UserService
) extends Controller with LoginLogout with AuthConfigImpl{

  val loginForm = Form {
    mapping(
      "email" -> email,
      "password" -> text
    )(userService.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action {
    Ok(views.html.login())
  }

  def authenticate = Action.async(parse.json){implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future {
          BadRequest(Json.obj("errors" -> formWithErrors.errors.head.message))
      },
      user => gotoLoginSucceeded(user.get.email)
    )
  }
}
