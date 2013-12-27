package controllers.application

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.application._
import play.api.data.validation._
import org.apache.commons.validator.routines.UrlValidator
import service.application.definitions._
import com.google.inject._
import scala.util.{Success, Failure}

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

class CRUDController @Inject()(applicationService: ApplicationService) extends Controller {

  protected case class ConstraintArgument(
    name: String,
    regexp: String,
    errorMessage: String
  )

  // regexp not working properly -.-
  val packageNameConstraint = new ConstraintArgument(
                                "packagenamecheck",
                                """^(([a-z])+.)+[A-Z]([A-Za-z])+$""",
                                "Package name in invalid"
                              )
  private def textCheckConstraint(args: Seq[ConstraintArgument]): Constraint[String] = Constraint("", args)({
    text => {
      val arg = args.head
      val regexp = arg.regexp.r
      val errors = text match {
        case regexp() => Nil
        case _ => Seq(ValidationError(arg.errorMessage))
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
    }
  })

  private def urlCheckConstraint: Constraint[String] = Constraint("")({
    text => {
      val urlValidator = new UrlValidator()
      if(urlValidator.isValid(text)){
        Valid
      } else {
        Invalid("Url is invalid")
      }
    }
  })

  private def applicationNameConstrait: Constraint[String] = Constraint(""){
    text => {
      if(applicationService.exists(text)){
        Invalid("An application with this name already exists")
      } else {
        Valid
      }
    }
  }

  val applicationForm: Form[WazzaApplication] = Form(
    mapping(
      "name" -> nonEmptyText.verifying(applicationNameConstrait),
      "appUrl" -> nonEmptyText.verifying(urlCheckConstraint),
      "storeId" -> nonEmptyText,
      "packageName" -> nonEmptyText.verifying(textCheckConstraint(Seq(packageNameConstraint))),
      "appType" -> optional(text),
      "sdk" -> mapping(
        "apiKey" -> nonEmptyText
      )(SdkVariables.apply)(SdkVariables.unapply),
      "items" -> ignored(List[Item]())
    )(WazzaApplication.apply)(WazzaApplication.unapply)
  )

  def newApplication = Action { implicit request =>
    Ok(views.html.newApplication(applicationForm))
  }

  def newApplicationSubmit = Action { implicit request =>
    applicationForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.newApplication(errors))
      },
      application => {
        val result = applicationService.insertApplication(application)
        
        result match {
          case Success(app) => Redirect("/")
          case Failure(_) => BadRequest("/")
        }
      }
    )
  }
}
