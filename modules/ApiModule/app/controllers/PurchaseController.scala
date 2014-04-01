package controllers.api

import com.google.inject._
import models.application.Item
import models.user.DeviceInfo
import models.user.LocationInfo
import models.user.PurchaseInfo
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
import service.user.definitions.{PurchaseService}

class PurchaseController @Inject()(
  applicationService: ApplicationService,
  purchaseService: PurchaseService
) extends Controller {

  
  def handlePurchase = Action(parse.json) {implicit request =>
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
    if(applicationService.itemExists(
      (content \ "itemId").as[String],
      (content \ "name").as[String]
    )) {

      val userId =  (content \ "userId").as[String]
      val purchaseInfo = new PurchaseInfo(
        (content \ "id").as[String],
        userId,
        (content \ "name").as[String],
        (content \ "itemId").as[String],
        (content \ "price").as[Double],
        (content \ "time").as[String],
        (content \ "deviceInfo").as[DeviceInfo],
        (content \ "location").validate[LocationInfo] match {
          case success: JsSuccess[LocationInfo] => Some(success.value)
          case JsError(errors) => None
        }
      )
      purchaseService.save(purchaseInfo, userId) match {
        case Success(_) => Ok
        case Failure(_) => BadRequest
      }
    } else {
      BadRequest
    }
  }

}

