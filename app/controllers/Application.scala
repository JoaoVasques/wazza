package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import com.google.inject._

class Application extends Controller with Security{

  def index = Action {
	 Ok(views.html.index())
  }

  def launch = Action {
	 Ok(views.html.launch())
  }

  def fault(path : String) = Action {
  	if(path == "home")
  		Ok(views.html.index2())
  	else
  		Ok(views.html.index())
  }
}
