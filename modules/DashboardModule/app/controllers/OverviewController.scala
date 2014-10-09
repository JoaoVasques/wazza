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

class OverviewController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller with Security{

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

}
