package controllers.api

import com.google.inject._
import models.user.MobileUser
import org.bson.types.ObjectId
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
import service.persistence.definitions.DatabaseService
import service.user.definitions.{MobileUserService}

class RecommendationController @Inject()(
  recommendationService: RecommendationService,
  userService: MobileUserService,
  databaseService: DatabaseService
) extends Controller {

  def recommendItemsToUser(
    companyName: String,
    applicationName: String,
    userId: String,
    limit: Int = -1
  ) = Action.async {implicit request =>

    val id = databaseService.get(
      MobileUser.getCollection(companyName, applicationName),
      MobileUser.KeyId,
      userId,
      "_id") match {
      case Some(json) => {
        (json \ "_id" \ "$oid").as[String]
      }
      case None => null
    }

    if(id != null) {
      recommendationService.recommendItemsToUser(companyName, applicationName, id, limit) map { result =>
        Ok(result)
      } recover {
        case err: Exception => BadRequest(err.getMessage())
      }
    } else {
      Future { BadRequest(s"User $userId does not exist") }
    }
  }
}

