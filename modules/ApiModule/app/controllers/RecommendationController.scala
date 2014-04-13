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

class RecommendationController @Inject()(
  recommendationService: RecommendationService
) extends Controller {

  def test = Action{ request =>
    Ok
  }
}
