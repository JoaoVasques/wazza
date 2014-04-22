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

  
  def handlePurchase(companyName: String, applicationName: String) = Action(parse.json) {implicit request =>
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
    if(applicationService.itemExists(
      companyName,
      (content \ "itemId").as[String],
      applicationName
    )) {

      val userId =  (content \ "userId").as[String]
      val purchaseInfo = purchaseService.create(content)
      purchaseService.save(companyName, applicationName, purchaseInfo, userId) match {
        case Success(_) => Ok
        case Failure(_) => BadRequest
      }
    } else {
      BadRequest
    }
  }

}

