package controllers.api

import com.google.inject._
import models.application.Item
import models.user.LocationInfo
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
  applicationService: ApplicationService
) extends Controller with ApiSecurity {

  private lazy val OffsetHeader = "Offset"

  private def getOffsetValue[A](request: Request[A]): Int = {
    request.headers.get(OffsetHeader) match {
      case Some(o) => o.toInt
      case None => 0
    }
  }

  private def getItemsAux[A](
    request: Request[A],
    companyName: String,
    applicationName: String,
    projection: String = null
  ): List[Any] = {
    applicationService.getItems(companyName, applicationName, getOffsetValue(request))
  }

  def getItems(companyName: String, applicationName: String) = ApiSecurityHandler() {implicit request =>
    val result = applicationService.getItems(companyName, applicationName, getOffsetValue(request))
    Ok(JsArray(result.map((item: Item) =>{
      Json.obj("id" -> item.name)
    })))
  }

  def getItemsWithDetails(companyName: String, applicationName: String) = ApiSecurityHandler() {implicit request =>
    val result = applicationService.getItems(companyName, applicationName, getOffsetValue(request))
    Ok(JsArray(result.map{(item: Item) =>
      Item.convertToJson(item)
    }))
  }
 
  def getItemDetails(
    companyName: String,
    applicationName:String,
    id: String
  ) = ApiSecurityHandler() {implicit request =>
    val res = applicationService.getItem(companyName, id, applicationName)
    Ok(Json.obj(
      "item" ->  res.map{item => Item.convertToJson(item)}
    ))
  }
}

