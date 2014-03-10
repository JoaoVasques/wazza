package controllers.dashboard

import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import service.user.definitions._
import com.google.inject._
import play.api.libs.json._
import models.application.{Item, VirtualCurrency, Credentials}

class DashboardController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller with Security{

  def index() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty){
      Ok(views.html.dashboard(false, "", null, Nil, Nil))
    } else {
      request.body.asJson match {
        case Some(json) => {
          Ok("skip " + json)
        }
        case None => {
          val application = applicationService.find(applications.head).get
          Ok(views.html.dashboard(
            true,
            application.name,
            application.credentials,
            application.virtualCurrencies,
            application.items
          ))
        }
      }
    }
  }

  // add optional argument: application name
  def bootstrapDashboard() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty){
      //TODO: do not send bad request but a note saying that we dont have applications. then redirect to new application page
      BadRequest
    } else {
      val application = applicationService.find(applications.head).get
      val user = userService.find(userId).get
      Ok(
        Json.obj(
          "name" -> application.name,
          "userInfo" -> Json.obj(
            "name" -> user.name,
            "email" -> user.email
          ),
          "credentials" -> Json.obj(
            "apiKey" -> application.credentials.apiKey,
            "sdkKey" -> application.credentials.sdkKey
          ),
          "virtualCurrencies" -> new JsArray(application.virtualCurrencies map {vc =>
            VirtualCurrency.buildJson(vc)
          }),
          "items" -> new JsArray(applicationService.getItems(application.name) map {item =>
            Item.convertToJson(item)
          }),
          "applications" -> new JsArray(applications map {el =>
            Json.obj("name" -> el)
          })
        )
      )
    }
  }

  def analyticsSection = HasToken() {token => userId => implicit request =>
    Ok(views.html.analytics())
  }

  def storeAndroid = HasToken() {token => userId => implicit request =>
    Ok(views.html.storeAndroid())
  }

  def storeApple = HasToken() {token => userId => implicit request =>
    Ok(views.html.storeApple())
  }

  def storeAmazon = HasToken() {token => userId => implicit request =>
    Ok(views.html.storeAmazon())
  }

  def settingsSection = HasToken() {token => userId => implicit request =>
    Ok(views.html.settings())
  }

  def inventory = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventory())
  }

  def inventoryCRUD = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventoryCRUD())
  }

  def inventoryVirtualCurrencies = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventoryVirtualCurrencies())
  }

}
