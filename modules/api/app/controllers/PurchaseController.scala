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
import controllers.security._

class PurchaseController @Inject()(
  applicationService: ApplicationService,
  purchaseService: PurchaseService
) extends Controller {

  def handlePurchase() = ApiSecurityAction.async(parse.json) {implicit request =>
    val companyName = request.companyName
    val applicationName = request.applicationName
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
    //TODO fetch company and app's name and add it to content
    applicationService.exists(companyName, applicationName) flatMap {exists =>
      if(!exists) {
        Future.successful(NotFound("Application does not exist"))
      } else {
        val purchase = purchaseService.create(content)
        purchaseService.save(companyName, applicationName, purchase) map {res =>
          Ok
        } recover {
          case _ => InternalServerError
        }
      }
    }
  }
}

