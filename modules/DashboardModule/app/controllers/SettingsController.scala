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

class SettingsController @Inject()(
	applicationService: ApplicationService,
	userService: UserService
	) extends Controller {

	def bootstrap() = UserAuthenticationAction.async {implicit request =>
		userService.getApplications(request.userId) flatMap {applications =>
			userService.find(request.userId) flatMap {userOpt =>
				val user = userOpt.get
				val companyName = user.company
				val info = applicationService.find(companyName, applications.head) map {optApp =>
					(optApp map {application =>
						Json.obj(
							"userInfo" -> Json.obj(
								"name" -> user.name,
								"email" -> user.email
								),
							"credentials" -> Json.obj(
								"sdkToken" -> application.credentials.sdkToken
								)
							)
						}).get
				}
				info map {Ok(_)}
			}
		}
	} 

	def settings = UserAuthenticationAction {implicit request =>
		Ok(views.html.settings())
	}
}

