/**
  NOT USED AT THE MOMENT
  
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


class InventoryController @Inject()(
	applicationService: ApplicationService,
	userService: UserService
	) extends Controller {

	def bootstrapInventory() = UserAuthenticationAction.async {implicit request =>
	    userService.getApplications(request.userId) flatMap {applications =>
	        if(applications.isEmpty){
	            //TODO: do not send bad request but a note saying that we dont have applications. then redirect to new application page
	            Future.successful(Forbidden)
	        } else {
	            userService.find(request.userId) flatMap {userOpt =>
	                val user = userOpt.get
	                val companyName = user.company
	                val info = applicationService.find(companyName, applications.head) map {optApp =>
	                    (optApp map {application =>
	                        Json.obj(
	                            "virtualCurrencies" -> new JsArray(application.virtualCurrencies map {vc =>
	                                VirtualCurrency.buildJson(vc)
	                            })/**,
	                            "items" -> new JsArray(applicationService.getItems(companyName, application.name) map {item =>
	                                Item.convertToJson(item)
	                            })**/
	                        )
	                    }).get
	                }
	                info map {Ok(_)}
	            }
	        }
	    }
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

}

  * */
