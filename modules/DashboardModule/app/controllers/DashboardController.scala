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

class DashboardController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller with Security{

  lazy private val ItemBatch = 10


  //NOTE: this is being called two times when the session is on. check why
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
            applicationService.getItems(application.name, ItemBatch)
          ))
        }
      }
    }
  }
}
