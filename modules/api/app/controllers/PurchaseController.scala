package controllers.api

import com.google.inject._
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import service.security.definitions.TokenManagerService
import service.user.definitions._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import user._
import user.messages._
import scala.collection.mutable.Stack
import scala.util.{Try, Success, Failure}
import models.user.{PurchaseInfo}
import payments.paypal._

class PurchaseController @Inject()(
  paypalService: PayPalService
) extends Controller {

  def handlePurchase() = ApiSecurityAction.async(parse.json) {implicit request =>
    val companyName = request.companyName
    val applicationName = request.applicationName

    try {
    val content = Json.parse((request.body \ "content").as[String].replace("\\", ""))
      val userProxy = UserProxy.getInstance()
      val req = new PRSave(new Stack, companyName, applicationName, content)
      userProxy ! req
      Future.successful(Ok)
    } catch {
      case _: Throwable => {
        Future.successful(BadRequest("Invalid purchase json description"))
      }
    }
  }

  def verifyPayment() = Action.async(parse.json) {implicit request =>
    //TODO
    println(request.body)
    val paymentID = (request.body \ "responseID").as[String]
    val amount = (request.body \ "price").as[Double]
    val currency = (request.body \ "currencyCode").as[String]
    val clientID = (request.body \ "apiClientId").as[String]
    val secret = (request.body \ "apiSecret").as[String]

    for {
      token <- paypalService.getAccessToken(clientID, secret)
      valid <- paypalService.verifyPayment(token, paymentID, amount, currency)
    } yield valid

    Future {
      Ok(Json.obj("result" -> "OK"))
    }
  }
}

