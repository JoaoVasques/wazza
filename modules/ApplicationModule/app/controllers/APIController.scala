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
) extends Controller {

  def getVirtualCurrencies(applicationName: String, companyName: String) = Action.async { implicit request =>
    applicationService.getVirtualCurrencies(companyName, applicationName) map { res =>
      Ok(new JsArray(res map {vc =>
        VirtualCurrency.buildJson(vc)
      }))
    }
  }

  def getItems(
    applicationName: String,
    companyName: String,
    offset: Int
  ) = Action.async { implicit request =>
    applicationService.getItems(companyName, applicationName, offset) map {items =>
      Ok(new JsArray(items map {item =>
        Item.convertToJson(item)
      }))
    }
  }
}
