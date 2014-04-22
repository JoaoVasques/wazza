package controllers.api

import com.google.inject._
import models.user.MobileUser
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import scala.concurrent._
import ExecutionContext.Implicits.global
import service.definitions.recommendation.{RecommendationService}
import service.user.definitions.{MobileUserService}

class RecommendationController @Inject()(
  recommendationService: RecommendationService,
  userService: MobileUserService
) extends Controller {

  def recommendItemsToUser(
    companyName: String,
    applicationName: String,
    userId: String,
    limit: Int = -1
  ) = Action.async {implicit request =>

    userService.get(companyName, applicationName, userId) match {
      case Some(user) => {
        recommendationService.recommendItemsToUser(companyName, applicationName, userId, limit) map { result =>
          Ok(result)
        } recover {
          case err: Exception => BadRequest(err.getMessage())
        }
      }
      case None => Future {BadRequest("user does not exist")}
    }
  }
}

