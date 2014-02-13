package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def dashboard = Action {
  	Ok(views.html.dashboard("Your new application is ready."))
  }
}
