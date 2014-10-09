package controllers.api

import com.google.inject._
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import service.application.definitions.ApplicationService
import service.security.definitions.TokenManagerService
import service.user.definitions.{PurchaseService}
import scala.concurrent._
import ExecutionContext.Implicits.global

class PurchaseController @Inject()(
  applicationService: ApplicationService,
  purchaseService: PurchaseService
) extends Controller {

  def handlePurchase(companyName: String, applicationName: String) = Action.async(parse.json) {implicit request =>
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
    applicationService.itemExists(companyName, (content \ "itemId").as[String], applicationName) flatMap {exist =>
      if(exist) {
        val purchaseInfo = purchaseService.create(content)
        purchaseService.save(companyName, applicationName, purchaseInfo) map {res =>
          Ok
        } recover {
          case _ => BadRequest
        }
      } else {
        Future { BadRequest("Item does not exist") }
      }
    }
  }
}

