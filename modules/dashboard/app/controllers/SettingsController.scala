package controllers.dashboard

import play.api._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import com.google.inject._
import play.api.libs.json._
import models.application._
import java.util.Date
import akka.util.{Timeout}
import akka.pattern.ask
import user._
import user.messages._
import application._
import application.messages._
import scala.concurrent.duration._
import scala.collection.mutable.Stack
import payments.{PaymentTypes}

class SettingsController  extends Controller {

  private implicit val timeout = Timeout(10 seconds)
  private val userProxy = UserProxy.getInstance()
  private val appProxy = ApplicationProxy.getInstance()

	def bootstrap(appName: String) = UserAuthenticationAction.async {implicit request =>
    val appsFuture = (userProxy ? new URGetApplications(new Stack, request.userId, true)).mapTo[URApplicationsResponse]
    appsFuture flatMap {applications =>
      val userFuture = (userProxy ? new URFind(new Stack, request.userId, true)).mapTo[UROptionResponse]
		  userFuture flatMap {userOpt =>
				val user = userOpt.res.get
				val companyName = user.company
				val application = applications.res.find(_ == appName).get
        val futureApp = (appProxy ? new ARFind(new Stack, companyName, appName, true)).mapTo[AROptionResponse]
				val info = futureApp map {optApp =>
					(optApp.res map {application =>
						val result = Json.obj(
							"userInfo" -> Json.obj(
								"name" -> user.name,
								"email" -> user.email
								),
							"credentials" -> Json.obj(
								"sdkToken" -> application.credentials.sdkToken
								)
							)
            application.paypalCredentials match {
              case Some(credentials) => {
                result ++ Json.obj("payPalCredentials" -> Json.toJson(credentials))
              }
              case None => result
            }
						}).get
				}
				info map {Ok(_)}
			}
		}
	}

	def settings = UserAuthenticationAction {implicit request =>
		Ok(views.html.settings())
	}

  def updatePaymentsCredentialsController(
    companyName: String,
    applicationName: String
  ) = UserAuthenticationAction.async(parse.json) {implicit request =>
    try {
      (request.body \ "paymentSystem").as[Int] match {
        /** PayPal **/
        case 2 => {
          request.body.validate[PayPalCredentials] match {
            case credentials: JsSuccess[PayPalCredentials] => {
              appProxy ! new ARAddPayPalCredentials(new Stack, companyName, applicationName, credentials.get, true)
              appProxy ! new ARAddPaymentSystem(new Stack, companyName, applicationName, PaymentTypes.PayPal)
            }
            case _: JsError => Future.successful(BadRequest)
          }
        }
      }      

      Future.successful(Ok)
    } catch {
      case ex: Exception => Future.successful(BadRequest)
    }
  }
}

