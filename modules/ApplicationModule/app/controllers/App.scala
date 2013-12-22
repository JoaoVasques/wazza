package controllers.application

import play.api._
import play.api.mvc._

object AppController extends Controller {

  def index = Action {
    Ok("Hello app")
  }

}
