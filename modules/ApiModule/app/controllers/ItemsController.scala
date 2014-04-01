package controllers.api

import com.google.inject._
import models.application.Item
import models.application.LocationInfo
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import service.application.definitions.ApplicationService

class ItemsController @Inject()(
  applicationService: ApplicationService,
  purchaseService: PurchaseService
) extends Controller with ApiSecurity {

  private lazy val OffsetHeader = "Offset"

  private def getOffsetValue[A](request: Request[A]): Int = {
    request.headers.get(OffsetHeader) match {
      case Some(o) => o.toInt
      case None => 0
    }
  }

  private def getItemsAux[A](request: Request[A], applicationName: String, projection: String = null): List[Any] = {
    applicationService.getItems(applicationName, getOffsetValue(request))
  }

  def getItems(applicationName: String) = ApiSecurityHandler() {implicit request =>
    val result = applicationService.getItems(applicationName, getOffsetValue(request))
    Ok(JsArray(result.map((item: Item) =>{
      Json.obj("id" -> item.name)
    })))
  }
 
  def getItemDetails(id: String, applicationName: String) = ApiSecurityHandler() {implicit request =>
    val res = applicationService.getItem(id, applicationName)
    Ok(Json.obj(
      "item" ->  res.map{item => Item.convertToJson(item)}
    ))
  }
}

