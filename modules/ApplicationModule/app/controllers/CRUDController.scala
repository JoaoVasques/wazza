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
import service.security.definitions._
import SecretGeneratorServiceContext._
import play.api.mvc.MultipartFormData._
import service.photos.definitions._
import play.api.i18n.Messages

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

class CRUDController @Inject()(
    applicationService: ApplicationService,
    secretGeneratorService: SecretGeneratorService,
    uploadPhotoService: UploadPhotoService
  ) extends Controller {

  // regexp not working properly -.-
  private def checkPackageNameFormat(name: String): Boolean = {
    val regexp = """[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z0-9_]+)*""".r
    name match {
      case regexp() => true
      case _ => false
    }
  } 

  private def urlCheckConstraint: Constraint[String] = Constraint("")({
    text => {
      if(new UrlValidator().isValid(text)){
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
      "imageName" -> ignored(""),
      "packageName" -> text,
      "appType" -> optional(text),
      "credentials" -> mapping(
        "appId" -> ignored(secretGeneratorService.generateSecret(Id)),
        "apiKey" -> ignored(secretGeneratorService.generateSecret(ApiKey)),
        "sdkKey" -> ignored(secretGeneratorService.generateSecret(ApiKey))
      )(Credentials.apply)(Credentials.unapply),
      "items" -> ignored(List[Item]())
    )
    (WazzaApplication.apply)(WazzaApplication.unapply)
  )

  def newApplication = Action { implicit request =>
    Ok(views.html.newApplication(applicationForm, applicationService.getApplicationyTypes))
  }

  private def generateBadRequestResponse(form: Form[WazzaApplication]): Result = {
    BadRequest(views.html.newApplication(form, applicationService.getApplicationyTypes))
  }

  def newApplicationSubmit = Action { implicit request =>
    applicationForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.newApplication(errors, applicationService.getApplicationyTypes))
      },
      application => {

        if(application.appType.get == "Android" && (!checkPackageNameFormat(application.packageName))){
          generateBadRequestResponse(applicationForm.withError("packageName", "package name is invalid"))
        } else {
          val image = request.body.asMultipartFormData.get.file("image")
          image match {
            case Some(_) => {
              val uploadResult = uploadPhotoService.upload(image.get)

              if(uploadResult.isSuccess){
                application.imageName = uploadResult.get
                applicationService.insertApplication(application)
                Redirect("/")
              } else {
                generateBadRequestResponse(applicationForm.withError("image", "Image upload error. Please try again"))
              }
            }
            case None => {
              BadRequest("NO IMAGE")
            }
          }
        }
      }
    )
  }
}
