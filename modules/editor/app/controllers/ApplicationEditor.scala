package controllers.editor

import play.api._
import play.api.mvc._

object ApplicationEditor extends Controller {

  def index = Action {
    Ok("hello editor")
  }

}
