package controllers.user

import play.api._
import play.api.mvc._
import user.service._
import com.google.inject._

class ApplicationUser @Inject()(userService: UserService) extends Controller {

  def index = Action {
  	println(userService.findBy("", ""))
    Ok("Hello User")
  }

}
