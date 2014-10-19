package controllers.application

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.application._
import play.api.data.validation._
import org.apache.commons.validator.routines.UrlValidator
import service.application.definitions._
import service.aws.definitions.{PhotosService}
import com.google.inject._
import scala.util.{Success, Failure}
import service.security.definitions._
import SecretGeneratorServiceContext._
import play.api.mvc.MultipartFormData._
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import service.user.definitions._
import scala.language.implicitConversions
import play.api.libs.Files._
import java.io.File

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
  userService: UserService,
  photosService: PhotosService
) extends Controller {

  private def generateErrors(value: String) = {
    BadRequest(Json.obj("errors" -> value))
  }

  private def checkPackageNameFormat(name: String): Boolean = {
    if(name == null){
      false
    } else {
      val parts = name.split("[\\.]")
      if(parts.length == 0){
        false
      }

      for( part <- parts) {
        val iter = new StringCharacterIterator(part)
        var c = iter.first

        if((c == CharacterIterator.DONE) || 
          (!Character.isJavaIdentifierStart(c) && !Character.isIdentifierIgnorable(c))
        ){
          false
        }
        
        c = iter.next
        while (c != CharacterIterator.DONE) {
          if (!Character.isJavaIdentifierPart(c) && !Character.isIdentifierIgnorable(c)){
            false
          }
          c = iter.next();
        }
      }
      true
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

  val applicationForm: Form[WazzaApplication] = Form(
    mapping(
      "name" -> nonEmptyText,
      "url" -> ignored(""),//nonEmptyText.verifying(urlCheckConstraint),
      "imageName" -> nonEmptyText,
      "packageName" -> ignored("com.test"),
      "appType" -> list(text),
      "credentials" -> mapping(
        "appId" -> ignored(secretGeneratorService.generateSecret(Id)),
        "apiKey" -> ignored(secretGeneratorService.generateSecret(ApiKey)),
        "sdkKey" -> ignored(secretGeneratorService.generateSecret(ApiKey))
      )(Credentials.apply)(Credentials.unapply),
      "items" -> ignored(List[Item]()),
      "virtualCurrencies"-> ignored(List[VirtualCurrency]())
    )
    (WazzaApplication.apply)(WazzaApplication.unapply)
  )

  def newApplication = UserAuthenticationAction {implicit request =>
    Ok(views.html.newApplication(applicationService.getApplicationyTypes))
  }

  private def generateBadRequestResponse(errors: Form[WazzaApplication]): SimpleResult = {
    BadRequest(Json.obj("errors" -> errors.errorsAsJson))
  }

  def newApplicationSubmit(companyName: String) = UserAuthenticationAction.async(parse.json) {implicit request =>
    applicationForm.bindFromRequest.fold(
      errors => {
        Future {generateBadRequestResponse(errors) }
      },
      application => {
        applicationService.insertApplication(companyName,application) flatMap {app =>
          userService.addApplication(request.userId, app.name) map {r =>
            Redirect("/dashboard")
          }
        }recoverWith {
          case _ => Future.successful(BadRequest("Error while creating application"))
        }
      }
    )
  }

  private implicit def extractFile(filePart: FilePart[_]): File = {
    filePart.ref match {
      case TemporaryFile(file) => file
    }
  }

  def uploadImage() = UserAuthenticationAction.async(parse.multipartFormData) { implicit request =>
    photosService.upload(request.body.files.head) map { photoResult =>
      Ok(photoResult.toJson)
    } recover {
      case error => {
        generateErrors(error.getCause.getMessage)
      }
    }
  }

}
