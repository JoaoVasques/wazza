package controllers.dashboard

import play.api._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import service.user.definitions._
import com.google.inject._
import play.api.libs.json._
import models.application._
import java.util.Date

class AnalyticsController @Inject()(
	applicationService: ApplicationService,
	userService: UserService
	) extends Controller {

	def analytics = UserAuthenticationAction {implicit request =>
		Ok(views.html.analytics.generic())
	}

/** not used atm
  //store
  def storeAndroid = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeAndroid())
  }

  def storeApple = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeApple())
  }

  def storeAmazon = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeAmazon())
  }
**/

  //inventory
  def inventory = UserAuthenticationAction {implicit request =>
  	Ok(views.html.inventory.inventory())
  }

  def inventoryCRUD = UserAuthenticationAction {implicit request =>
  	Ok(views.html.inventory.inventoryCRUD())
  }

  def inventoryVirtualCurrencies = UserAuthenticationAction {implicit request =>
  	Ok(views.html.inventory.inventoryVirtualCurrencies())
  }

  //others

  def settingsSection = UserAuthenticationAction {implicit request =>
  	Ok(views.html.settings())
  }
}

