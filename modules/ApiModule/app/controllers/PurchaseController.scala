package controllers.api

import com.google.inject._
import models.application.Item
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

