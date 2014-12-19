
/**
  NOT USED AT THE MOMENT

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
) extends Controller {

  private lazy val OffsetHeader = "Offset"

  private def getOffsetValue[A](request: Request[A]): Int = {
    request.headers.get(OffsetHeader) match {
      case Some(o) => o.toInt
      case None => 0
    }
  }

  def getItems(companyName: String, applicationName: String) = Action.async {implicit request =>
    val futureResult = applicationService.getItems(companyName, applicationName, getOffsetValue(request))
    futureResult map {items =>
      Ok(new JsArray(items map {i => Json.obj("id" -> i.name)}))
    }
  }

  def getItemsWithDetails(companyName: String, applicationName: String) = Action.async  {implicit request =>
    val futureResult = applicationService.getItems(companyName, applicationName, getOffsetValue(request))
    futureResult map {items =>
      Ok(new JsArray(items map {i =>
        Item.convertToJson(i)
      }))
    }
  }
 
  def getItemDetails(
    companyName: String,
    applicationName:String,
    id: String
  ) = Action.async {implicit request =>
    val futureRes = applicationService.getItem(companyName, id, applicationName)

    futureRes map {opt =>
      Ok((opt map {i =>
        Json.obj("item" -> Item.convertToJson(i))
      }).get)
    }
  }
}

  * */
