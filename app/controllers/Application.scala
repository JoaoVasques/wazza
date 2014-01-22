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

class Application @Inject()(
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

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action {
    Ok(views.html.login())
  }

  def test = HasToken() { token => userId => implicit request =>
    println(token)
    println(userId)
    Ok("TEST")
  }

  def authenticate = Action.async(parse.json){implicit request =>
    def getCookieInfo(result: SimpleResult) = {
      val tokenInfo = result.header.headers("Set-Cookie").split(";").head.split("=")
      Json.obj("tokenName" -> tokenInfo.head, "tokenValue" -> tokenInfo.tail.filter(_ != '\"'))
    }

    loginForm.bindFromRequest.fold(
      formWithErrors => Future {
          BadRequest(Json.obj("errors" -> formWithErrors.errors.head.message))
      },
      user => {
        val token = tokenService.startNewSession(user.get.email)
        Future {
          Ok(Json.obj(
            "authToken" -> token,
            "userId" -> user.get.email
          )).withToken(token)
        }
      }
    )
  }
}
