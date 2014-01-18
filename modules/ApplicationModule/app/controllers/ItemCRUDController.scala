package controllers.application

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.application._
import com.google.inject._
import service.application.definitions._
import play.api.data.format.Formats._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}
import scala.concurrent._
import ExecutionContext.Implicits.global
import models.aws._
import service.aws.definitions.{UploadFileService}
import play.api.libs.Files._
import java.io.File
import play.api.mvc.MultipartFormData._
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

class ItemCRUDController @Inject()(
    applicationService: ApplicationService,
    itemService: ItemService,
    uploadFileService: UploadFileService
  ) extends Controller {

  private def generateErrors(value: String) = {
    BadRequest(Json.obj("errors" -> value))
  }
  
  def newItem(storeType: String) = Action { implicit request =>
    if(applicationService.getApplicationyTypes.contains(storeType)){
      Ok(views.html.newItem(storeType, List("Real", "Virtual")))
    } else {
      generateErrors("Unknown store type")
    }
  }

  def newItemSubmit(applicationName: String) = Action.async(parse.multipartFormData) { implicit request =>
    val result = itemService.createItemFromMultipartData(request.body, applicationName)

    result map {data =>
      data match {
        case Success(s) => Ok
        case Failure(f) => generateErrors(f.getMessage)
      }
    } recover {
      case err: S3Failed => generateErrors("Problem uploading image to server")
      case err: Exception => generateErrors((if(err.getMessage != null) err.getMessage else err.getCause.getMessage))
    }
  }

  def uploadImage() = Action.async(parse.multipartFormData) { implicit request =>
    val imageUploadResult = uploadFileService.upload(request.body.files.head)

    imageUploadResult map { photoResult =>
      Ok(photoResult.toJson)
    } recover {
      case error => {
        generateErrors(error.getCause.getMessage)
      }
    }
  }

  private implicit def extractFile(filePart: FilePart[_]): File = {
    filePart.ref match {
       case TemporaryFile(file) => file
    }
  }
}
