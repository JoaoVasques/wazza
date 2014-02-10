package controllers.api

import com.google.inject._
import models.application.Item
import models.application.{PurchaseInfo}
import models.application.{LocationInfo}
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import service.application.definitions.ApplicationService
import service.security.definitions.TokenManagerService
import service.application.definitions.{PurchaseService}

class ItemsController @Inject()(
  applicationService: ApplicationService,
  purchaseService: PurchaseService
) extends Controller {

  def getItems(applicationName: String) = Action {implicit request =>
    val offset = request.headers.get("Offset") match {
      case Some(o) => {
        o.toInt
      }
      case None => 0
    }

    val result = applicationService.getItems(applicationName, offset)
    Ok(JsArray(result.map((item: Item) =>{
      Json.obj("id" -> item.name)
    })))
  }

  def getItemDetails(id: String, applicationName: String) = Action {implicit request =>
    val res = applicationService.getItem(id, applicationName)
    Ok(Json.obj(
      "item" -> res.map{item => Json.parse(Item.toCompactJson(item))}
    ))
  }
  
  def handlePurchase = Action(parse.json) {implicit request =>

    if(applicationService.itemExists(
      (request.body \ "itemId").as[String],
      (request.body \ "applicationName").as[String]
    )) {

      val purchaseInfo = new PurchaseInfo(
        (request.body \ "id").as[String],
        (request.body \ "applicationName").as[String],
        (request.body \ "itemId").as[String],
        (request.body \ "price").as[Double],
        (request.body \ "time").as[String],
        (request.body \ "location").validate[LocationInfo] match {
          case success: JsSuccess[LocationInfo] => Some(success.value)
          case JsError(errors) => None
        }
      )
      purchaseService.save(purchaseInfo) match {
        case Success(_) => Ok
        case Failure(_) => BadRequest
      }
    } else {
      BadRequest
    }
  }
}

