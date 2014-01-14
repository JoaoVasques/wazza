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
    applicationService: ApplicationService
  ) extends Controller {

  def getVirtualCurrencies(applicationName: String) = Action { implicit request =>
    println("hello...")
    val res = applicationService.getVirtualCurrencies(applicationName).map((vc: VirtualCurrency) => {
      VirtualCurrency.buildJson(vc)
    })
    println(res)
    Ok
  }
}
