package controllers.application

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.application._
import play.api.data.validation._
import org.apache.commons.validator.routines.UrlValidator
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
import scala.language.implicitConversions
import play.api.libs.Files._
import java.io.File
import application._
import application.messages._
import user._
import user.messages._
import scala.collection.mutable.Stack
import payments.{PaymentTypes}

class CRUDController @Inject()(
  secretGeneratorService: SecretGeneratorService,
  photosService: PhotosService
) extends Controller {

  private lazy val appProxy = ApplicationProxy.getInstance()
  private lazy val userProxy = UserProxy.getInstance() 

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
        "sdkToken" -> ignored(secretGeneratorService.generateSecret(ApiKey))
      )(Credentials.apply)(Credentials.unapply),
      "paypalCredentials" -> ignored(None.asInstanceOf[Option[PayPalCredentials]]),
      "paymentSystems" -> ignored(List[Int](PaymentTypes.InAppPurchases)),
      "items" -> ignored(List[Item]()),
      "virtualCurrencies"-> ignored(List[VirtualCurrency]())
    )
    (WazzaApplication.apply)(WazzaApplication.unapply)
  )

  def newApplication = UserAuthenticationAction {implicit request =>
    Ok(views.html.newApplication(WazzaApplication.applicationTypes))
  }

  private def generateBadRequestResponse(errors: Form[WazzaApplication]): Result = {
    BadRequest(Json.obj("errors" -> errors.errorsAsJson))
  }

  def newApplicationSubmit(companyName: String) = UserAuthenticationAction.async(parse.json) {implicit request =>
    applicationForm.bindFromRequest.fold(
      errors => {
        Future.successful(generateBadRequestResponse(errors))
      },
      application => {
        val appInsertRequest = new ARInsert(new Stack, companyName, application, true)
        appProxy ! appInsertRequest

        val userAddAppRequest = new URAddApplication(new Stack, request.userId, application.name, true)
        userProxy ! userAddAppRequest
        Future.successful(Redirect("/home"))
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
