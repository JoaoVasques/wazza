package controllers.application


import play.api._
import play.api.mvc._
import play.api.data._
import com.google.inject._
import service.application.definitions._
import play.api.data.format.Formats._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}
import models.application._
import scala.concurrent._
import ExecutionContext.Implicits.global
import service.security.definitions.{TokenManagerService}
import play.api.libs.json._
import controllers.security.{Security}
import service.user.definitions.{UserService}

/** Uncomment the following lines as needed **/
/**
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

class APIController @Inject()(
    applicationService: ApplicationService,
    userService: UserService
  ) extends Controller with Security {

  def getVirtualCurrencies(applicationName: String, companyName: String) = Action.async { implicit request =>
    Future {
      Json.toJson(applicationService.getVirtualCurrencies(companyName, applicationName).map((vc: VirtualCurrency) => {
        VirtualCurrency.buildJson(vc)
      }))      
    }.map(res => Ok(res))
  }

  def getItems(applicationName: String, companyName: String, offset: Int) = HasToken() {token => userId => implicit request =>
    val items = applicationService.getItems(companyName, applicationName, offset)
    Ok(new JsArray(items map {item =>
      Item.convertToJson(item)
    }))
  }
}
