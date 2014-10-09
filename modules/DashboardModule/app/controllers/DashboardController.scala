package controllers.dashboard

import play.api._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.security._
import service.security.definitions.{TokenManagerService}
import service.application.definitions._
import service.user.definitions._
import com.google.inject._
import play.api.libs.json._
import models.application._
import java.util.Date

class DashboardController @Inject()(
  applicationService: ApplicationService,
  userService: UserService
) extends Controller with Security{

  def index() = UserAuthenticationAction.async {implicit request =>
    userService.getApplications(request.userId) flatMap {applications =>
      if(applications.isEmpty){
        Future.successful(Ok(views.html.dashboard(false, "", null, Nil, Nil)))
      } else {
        request.body.asJson match {
          case Some(json) => {
            Future.successful(Ok("skip " + json))
          }
          case None => {
            for {
              user <- userService.find(request.userId)
              application <- applicationService.find(user.get.company, applications.head)
            } yield {
              (application map {(app: WazzaApplication) =>
                Ok(views.html.dashboard(
                  true,
                  app.name,
                  app.credentials,
                  app.virtualCurrencies,
                  app.items
                ))
              }).get
            }
          }
        }
      }
    }
  }

  def bootstrapOverview() = UserAuthenticationAction.async {implicit request =>
    userService.getApplications(request.userId) flatMap {applications =>
      if(applications.isEmpty) {
        Future.successful(BadRequest)
      } else {
        userService.find(request.userId) flatMap {user =>
          val companyName = user.get.company
          val apps = Future.sequence(applications map {appId: String =>
            applicationService.find(companyName, appId) map {optApp =>
              (optApp map {application =>
                Json.obj(
                  "name" -> application.name,
                  "url" -> application.imageName,
                  "platforms" -> application.appType
                )
              }).get
            }
          })
          apps map {appsList =>
            Ok(new JsArray(appsList))
          }
        }
      }
    }
  }

  // add optional argument: application name
  def bootstrapDashboard() = UserAuthenticationAction.async {implicit request =>
    userService.getApplications(request.userId) flatMap {applications =>
      if(applications.isEmpty){
        //TODO: do not send bad request but a note saying that we dont have applications. then redirect to new application page
        Future.successful(BadRequest)
      } else {
        userService.find(request.userId) flatMap {userOpt =>
          val user = userOpt.get
          val companyName = user.company
          val info = applicationService.find(companyName, applications.head) map {optApp =>
            (optApp map {application =>
              Json.obj(
                "companyName" -> companyName,
                "name" -> application.name,
                "userInfo" -> Json.obj(
                  "name" -> user.name,
                  "email" -> user.email
                ),
                "credentials" -> Json.obj(
                  "apiKey" -> application.credentials.apiKey,
                  "sdkKey" -> application.credentials.sdkKey
                ),
                "virtualCurrencies" -> new JsArray(application.virtualCurrencies map {vc =>
                  VirtualCurrency.buildJson(vc)
                }),
                /**    "items" -> new JsArray(applicationService.getItems(companyName, application.name) map {item =>
                  Item.convertToJson(item)
                  }),**/
                "applications" -> new JsArray(applications map {el =>
                  Json.obj("name" -> el)
                })
              )
            }).get
          }
          info map {Ok(_)}
        }
      }
    }
  } 


  def overview() = UserAuthenticationAction.async {implicit request =>
    userService.getApplications(request.userId) flatMap {applications =>
      if(applications.isEmpty){
        Future.successful(Ok(views.html.overview(false, "", null, Nil, Nil)))
      } else {
        request.body.asJson match {
          case Some(json) => {
            Future.successful(Ok("skip " + json))
          }
          case None => {
            userService.find(request.userId) flatMap {optUser =>
              val companyName = optUser.get.company
              applicationService.find(companyName, applications.head) map {optApp =>
                val application = optApp.get
                Ok(views.html.overview(
                  true,
                  application.name,
                  application.credentials,
                  application.virtualCurrencies,
                  application.items
                ))
              }
            }
          }
        }
      }
    }
  }

  def kpi = UserAuthenticationAction {implicit request =>
    Ok(views.html.kpi())
  }

  //analytics
  def analytics = UserAuthenticationAction {implicit request =>
    Ok(views.html.analytics.generic())
  }

  //store
  def storeAndroid = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeAndroid())
  }

  def storeApple = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeApple())
  }

  def storeAmazon = UserAuthenticationAction {implicit request =>
    Ok(views.html.store.storeAmazon())
  }

  //inventory
  def inventory = UserAuthenticationAction {implicit request =>
    Ok(views.html.inventory.inventory())
  }

  def inventoryCRUD = UserAuthenticationAction {implicit request =>
    Ok(views.html.inventory.inventoryCRUD())
  }

  def inventoryVirtualCurrencies = UserAuthenticationAction {implicit request =>
    Ok(views.html.inventory.inventoryVirtualCurrencies())
  }

  //others
  def campaigns = UserAuthenticationAction {implicit request =>
    Ok(views.html.campaigns())
  }

  def settingsSection = UserAuthenticationAction {implicit request =>
    Ok(views.html.settings())
  }
}

