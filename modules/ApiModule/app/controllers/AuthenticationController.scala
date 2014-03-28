package controllers.api

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import com.google.inject._

class AuthenticationController extends Controller with ApiSecurity {

  def authenticate = ApiSecurityHandler(parse.json) {implicit request =>

    Ok("TODO")
  }
}

