package controllers.user

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.user._
import com.mongodb.casbah.Imports._
import service.user.definitions._
import com.google.inject._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.libs.json._
import service.security.definitions.{TokenManagerService}
import controllers.security._

class RegistrationController @Inject()(
  userService: UserService,
  tokenService: TokenManagerService
) extends Controller with Security {

  val registrationForm : Form[User] = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> (nonEmptyText verifying email.constraints.head),
      "password" -> nonEmptyText,
      "company" -> nonEmptyText,
      "applications" -> ignored(List[String]())
    )(User.apply)(User.unapply) verifying("User with this email already exists", fields => fields match {
      case userData => userService.validateUser(userData.email)
    })
  )

  def registerUser = Action {
    Ok(views.html.registerUser(registrationForm))
  }

  def submitUser = Action.async(parse.json) { implicit request =>
    registrationForm.bindFromRequest.fold(
      formErrors => Future {
        BadRequest(Json.obj("errors" -> formErrors.errors.head.message))
      },
      user => Future {
        val token = tokenService.startNewSession(user.email)
        userService.insertUser(user)
        Ok(Json.obj(
          "authToken" -> token,
          "userId" -> user.email,
          "url" -> "analytics.overview"// TODO routes.Application.test().url
        )).withToken(token)
      }
    )
  }
}
