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
            applicationService.getItems(application.name, 0)
          ))
        }
      }
    }
  }

  // add optional argument: application name
  def bootstrapDashboard() = HasToken() {token => userId => implicit request =>
    val applications = userService.getApplications(userId)
    if(applications.isEmpty){
      BadRequest
    } else {
      val application = applicationService.find(applications.head).get
      Ok(
        Json.obj(
          "name" -> application.name,
          "credentials" -> Json.obj(
            "apiKey" -> application.credentials.apiKey,
            "sdkKey" -> application.credentials.sdkKey
          ),
          "virtualCurrencies" -> new JsArray(application.virtualCurrencies map {vc =>
            Json.parse(VirtualCurrency.toCompactJson(vc))
          }),
          "items" -> new JsArray(applicationService.getItems(application.name) map {item =>
            Json.parse(Item.toCompactJson(item))
          })
        )
      )
    }
  }

}
