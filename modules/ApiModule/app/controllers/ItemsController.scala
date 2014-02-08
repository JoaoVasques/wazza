package controllers.api

import com.google.inject._
import models.application.Item
import play.api._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import service.application.definitions.ApplicationService
import service.security.definitions.TokenManagerService

class ItemsController @Inject()(
  applicationService: ApplicationService
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
//    request.body.
    /**
      Information:
        - price: Double
        - time: String
        - location: {latitude, longitude}
    **/
    Ok
  }
}

