package controllers.application

import play.api._
import play.api.mvc._

class AppController extends Controller {

  def index = Action {
    Ok("Hello app")
  }

}
