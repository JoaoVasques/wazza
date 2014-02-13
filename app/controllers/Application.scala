package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}

class Application extends Controller with Security{

  def index = Action {
    Ok(views.html.index())
  }

  def dashboard = Action {
  	Ok(views.html.dashboard("Your new application is ready."))
  }

  def home = HasToken() { token => userId => implicit request =>
    Ok("TODO")
  }
}
