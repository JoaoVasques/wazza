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
    itemService: ItemService
  ) extends Controller {

  // It's giving a weird compilation error on the unapply method. Deal with this later
  // val googlePlayForm: Form[Item] = Form(
  //   mapping(
  //     "name" -> nonEmptyText,
  //     "description" -> nonEmptyText,
  //     "store" -> number,
  //     "metadata" -> mapping(
  //       "osType" -> ignored("Android"),
  //       "itemId" -> nonEmptyText,
  //       "title" -> ignored(""),
  //       "description" -> ignored(""),
  //       "publishedState" -> ignored(""),
  //       "purchaseType" -> number,
  //       "autoTranslate" -> ignored(false),
  //       "locale" -> ignored(List[GoogleTranslations]()),
  //       "autofill" -> ignored(false),
  //       "language" -> ignored(""),
  //       "price" -> of[Double]
  //     )(GoogleMetadata.apply)(GoogleMetadata.unapply),
  //     "currency" -> mapping(
  //       "typeOf" -> number,
  //       "value" -> of[Double]
  //     )(Currency.apply)(Currency.unapply)
  //   )
  //   (Item.apply)
  //   {
  //     item => Some(item.name,item.description,item.store,item.metadata,item.currency)
  //   }
  // )
  
  def newItem(storeType: String) = Action { implicit request =>
    if(applicationService.getApplicationyTypes.contains(storeType)){
      Ok(views.html.newItem(storeType, List("Real", "Virtual"), Nil))
    } else {
      BadRequest(Json.obj("errors" -> "Unknown store type"))
    }
  }

  def newItemSubmit(applicationName: String) = Action(parse.multipartFormData) { implicit request =>
    val result = itemService.createItemFromMultipartData(request.body, applicationName)
    result match {
      case Success(item) => Ok
      case Failure(errors) => {
        BadRequest(Json.obj("errors" -> errors.getMessage))
      }
    }
  }
}
