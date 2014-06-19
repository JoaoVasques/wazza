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
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import service.user.definitions._

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
    userService: UserService
  ) extends Controller with Security {

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
      "appUrl" -> nonEmptyText.verifying(urlCheckConstraint),
      "imageName" -> ignored(""),
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

  def newApplication = HasToken() { token => userId => implicit request =>
    Ok(views.html.newApplication(applicationService.getApplicationyTypes))
  }

  private def generateBadRequestResponse(errors: Form[WazzaApplication]): Result = {
    BadRequest(Json.obj("errors" -> errors.errorsAsJson))
  }

  def newApplicationSubmit(companyName: String) = HasToken(parse.json) { token => userId => implicit request =>
    applicationForm.bindFromRequest.fold(
      errors => {
        generateBadRequestResponse(errors)
      },
      application => {
        val result = applicationService.insertApplication(companyName,application)
        result match {
          case Success(app) => {
            userService.addApplication(userId, app.name)
            Redirect("/dashboard")
          }
          case Failure(f) => {
            BadRequest(f.getMessage)
          }
        }
      }
    )
  }
}
