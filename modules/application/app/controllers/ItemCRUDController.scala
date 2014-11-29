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
import service.aws.definitions.{PhotosService}
import play.api.libs.Files._
import java.io.File
import play.api.mvc.MultipartFormData._
import scala.language.implicitConversions
import controllers.security.{UserAuthenticationAction}

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
**/

class ItemCRUDController @Inject()(
  applicationService: ApplicationService,
  itemService: ItemService,
  photosService: PhotosService
) extends Controller {

  private def generateErrors(value: String) = {
    BadRequest(Json.obj("errors" -> value))
  }

  //TODO: add security
  def newItem = UserAuthenticationAction { implicit request =>
    Ok(views.html.newItem(List("Real", "Virtual")))
  }

  def newItemSubmit(
    companyName: String,
    applicationName: String
  ) = UserAuthenticationAction.async(parse.multipartFormData) { implicit request =>
    val futureResult = itemService.createItemFromMultipartData(companyName, request.body, applicationName)

    futureResult map {data =>
      val i: Item = null //TODO GET ITEM
      Ok.sendFile(
        content = itemService.generateMetadataFile(i),
        fileName = _ => s"$i.name.csv"
      )
    } recover {
      case err: S3Failed => {
        generateErrors("Problem uploading image to server")
      }
      case err: Exception => {
        println("exception: " + err.getCause.getMessage)
        generateErrors((if(err.getMessage != null) err.getMessage else err.getCause.getMessage))
      }
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

  def deleteItem(
    companyName: String,
    applicationName: String,
    itemId: String
  ) = UserAuthenticationAction.async(parse.json) { implicit request =>
    val imageName = (request.body \ "image").as[String]
    val result = applicationService.deleteItem(companyName, itemId, applicationName, imageName)
    result map {res =>
      Ok
    } recover {
      case err: Exception => generateErrors("Problem deleting item")
    }
  }

  private implicit def extractFile(filePart: FilePart[_]): File = {
    filePart.ref match {
      case TemporaryFile(file) => file
    }
  }
}
