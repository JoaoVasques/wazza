package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import com.google.inject._

class Application extends Controller {

  def index = Action {
	  Ok(views.html.index())
  }

  def home = Action {
    Ok(views.html.home())
  }

  def analyticsframe = Action {
    Ok(views.html.analyticsFrame())
  }

  def webframe = Action {
    Ok(views.html.webframe())
  }

  def notavailableyet = Action {
    Ok(views.html.notavailableyet())
  }

  def httpError = Action {
    Ok(views.html.errorPage())
  }

  def terms = Action {
    Ok(views.html.terms())
  }

}

