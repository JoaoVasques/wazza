package controllers.user

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.user._
import com.mongodb.casbah.Imports._
import service.user.definitions._
import com.google.inject._

class RegistrationController @Inject()(userService: UserService) extends Controller {

	val registrationForm : Form[User] = Form(
		mapping(
			"id" -> ignored(new ObjectId),
			"name" -> nonEmptyText,
			"email" -> (nonEmptyText verifying email.constraints.head),
			"password" -> nonEmptyText,
			"company" -> nonEmptyText
		)(User.apply)(User.unapply) verifying("User already exists", fields => fields match {
			case userData => userService.validateUser(userData.email)
		})
	)

	def registerUser = Action {
		Ok(views.html.registerUser(registrationForm))
	}

	def submitUser = Action { implicit request =>
		registrationForm.bindFromRequest.fold(
			errors => {
				BadRequest(views.html.registerUser(errors))
			},
			user => {
				userService.insertUser(user)
				Redirect("/")
			}
		)
	}
}
