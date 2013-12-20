package controllers.user

import play.api._
import play.api.mvc._
import user.models._

object ApplicationUser extends Controller {

  def index = Action {
  	val xpto = User.findBy("email", "test")
  	println(xpto)
    Ok("Hello User")
  }

}
