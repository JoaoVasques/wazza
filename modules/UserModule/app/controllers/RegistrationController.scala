package controllers.user

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.user._
import com.mongodb.casbah.Imports._
import service.user.definitions._
import com.google.inject._
/** Uncomment the following lines as needed **/
/**
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

class RegistrationController @Inject()(userService: UserService) extends Controller {

	val registrationForm : Form[User] = Form(
		mapping(
			"id" -> ignored(new ObjectId),
			"email" -> nonEmptyText,
			"password" -> nonEmptyText,
			"company" -> nonEmptyText
		)(User.apply)(User.unapply) verifying("Failed form constraints!", fields => fields match {
			case userData => userService.validateUser(userData.email)
		})
	)

	def registerUser = Action {
		Ok(views.html.registerUser(registrationForm))
	}

	def submitUser = TODO
}