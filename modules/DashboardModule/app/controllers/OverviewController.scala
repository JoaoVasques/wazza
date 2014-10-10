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

class OverviewController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller {

  def bootstrapOverview() = UserAuthenticationAction.async {implicit request =>
    userService.getApplications(request.userId) flatMap {applications =>
      if(applications.isEmpty) {
        Future.successful(Ok(new JsArray(List())))
      } else {
        userService.find(request.userId) flatMap {user =>
          val companyName = user.get.company
          val apps = Future.sequence(applications map {appId: String =>
            applicationService.find(companyName, appId) map {optApp =>
              (optApp map {application =>
                Json.obj(
                  "name" -> application.name,
                  "url" -> application.imageName,
                  "platforms" -> application.appType
                )
              }).get
            }
          })
          apps map {appsList =>
            Ok(new JsArray(appsList))
          }
        }
      }
    }
  }

  def overview() = UserAuthenticationAction {implicit request =>
    Ok(views.html.overview())
  }

}
