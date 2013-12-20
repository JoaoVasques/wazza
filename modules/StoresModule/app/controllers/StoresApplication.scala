package controllers.stores

import play.api._
import play.api.mvc._

object StoresApplication extends Controller {

  def index = Action {
    Ok(views.html.stores("stores"))
  }

}
