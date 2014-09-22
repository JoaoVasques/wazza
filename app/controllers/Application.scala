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

  def home = Action {
    Ok(views.html.home())
  }

  def webframe = Action {
    Ok(views.html.webframe())
  }

  def notavailableyet = Action {
    Ok(views.html.notavailableyet())
  }


}
