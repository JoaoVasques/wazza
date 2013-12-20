package controllers.user

import play.api._
import play.api.mvc._
import user.service.definitions._
import com.google.inject._

class ApplicationUser @Inject()(userService: UserService) extends Controller {

  def index = Action {
  	println(userService.exists("hey"))
    Ok("Hello User")
  }

}
