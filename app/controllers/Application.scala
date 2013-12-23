package controllers

import play.api._
import play.api.mvc._
import com.google.inject._
import models.application._
import service.application.definitions._

class Application @Inject()(applicationService: ApplicationService, itemService: ItemService) extends Controller {

  def index = Action {
  	runMiniTest
    Ok(views.html.index("Your new application is ready."))
  }

  def runMiniTest() = {
  	val app = new WazzaApplication("Test-application")
  	applicationService.insertApplication(app)

  	itemService.createGooglePlayItem(
  		"Test-application",
  		"id",
  		"title",
  		"name",
  		"description",
  		0,
  		1.29,
  		"publishedState",
  		0,
  		true,
  		true,
  		"language"
  	)
  }
}
