package controllers.api

import com.google.inject._
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json, JsValue}
import play.api.mvc._
import service.security.definitions.TokenManagerService
import service.user.definitions._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import scala.collection.mutable.Stack
import scala.util.{Try, Success, Failure}
import user._
import user.messages._
import models.common._

class AuthenticationController  extends Controller {

  val UserExistsHeader = "X-UserExists"

  def authentication() = ApiSecurityAction.async(parse.json) {implicit request =>
    val result = request.headers.get(UserExistsHeader) match {
      case Some(_) => {
        //Do nothing..
        false
      }
      case None => {
        //creates mobile user
        val req = new MUCreate(
          false,
          request.companyName,
          request.applicationName,
          (request.body \ "userId").as[String],
          (request.body \ "device").validate[DeviceInfo].asOpt.get,
          new Stack
        )
        UserProxy.getInstance() ! req
        true
      }
    }
    Future.successful(Ok(Json.obj("result" -> result)))
  }
}

