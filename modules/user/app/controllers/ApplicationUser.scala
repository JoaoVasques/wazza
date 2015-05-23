package controllers.user

import play.api._
import play.api.mvc._
import service.user.definitions._
import com.google.inject._

class ApplicationUser @Inject()(userService: UserService) extends Controller {}

