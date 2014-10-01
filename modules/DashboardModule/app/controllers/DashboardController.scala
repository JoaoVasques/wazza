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
import java.util.Date

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
          val companyName = userService.find(userId).get.company
          val application = applicationService.find(companyName, applications.head).get
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

  def bootstrapOverview() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty) {
      BadRequest
    } else {
      val user = userService.find(userId).get
      val companyName = user.company
      Ok(new JsArray(applications map {appId: String =>
        val application = applicationService.find(companyName, appId).get
        Json.obj(
          "name" -> application.name,
          "url" -> application.imageName,
          "platforms" -> application.appType
        )
      }))
    }
  }

  // add optional argument: application name
  def bootstrapDashboard() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty){
      //TODO: do not send bad request but a note saying that we dont have applications. then redirect to new application page
      BadRequest
    } else {
      val user = userService.find(userId).get
      val companyName = user.company
      val application = applicationService.find(companyName, applications.head).get
      Ok(
        Json.obj(
          "companyName" -> companyName,
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
          "items" -> new JsArray(applicationService.getItems(companyName, application.name) map {item =>
            Item.convertToJson(item)
          }),
          "applications" -> new JsArray(applications map {el =>
            Json.obj("name" -> el)
          })
        )
      )
    }
  } 


  def overview() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty){
      Ok(views.html.overview(false, "", null, Nil, Nil))
    } else {
      request.body.asJson match {
        case Some(json) => {
          Ok("skip " + json)
        }
        case None => {
          val companyName = userService.find(userId).get.company
          val application = applicationService.find(companyName, applications.head).get
          Ok(views.html.overview(
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

  def kpi = HasToken() {token => userId => implicit request =>
    Ok(views.html.kpi())
  }

  //analytics
  def analytics = HasToken() {token => userId => implicit request =>
    Ok(views.html.analytics.generic())
  }

  //store
  def storeAndroid = HasToken() {token => userId => implicit request =>
    Ok(views.html.store.storeAndroid())
  }

  def storeApple = HasToken() {token => userId => implicit request =>
    Ok(views.html.store.storeApple())
  }

  def storeAmazon = HasToken() {token => userId => implicit request =>
    Ok(views.html.store.storeAmazon())
  }

  //inventory
  def inventory = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventory.inventory())
  }

  def inventoryCRUD = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventory.inventoryCRUD())
  }

  def inventoryVirtualCurrencies = HasToken() {token => userId => implicit request =>
    Ok(views.html.inventory.inventoryVirtualCurrencies())
  }

  //others
  def campaigns = HasToken() {token => userId => implicit request =>
    Ok(views.html.campaigns())
  }

  def settingsSection = HasToken() {token => userId => implicit request =>
    Ok(views.html.settings())
  }

}
