package service.implementations.recommendation

import akka.util.Timeout
import models.user.MobileSession
import scala.util.Failure
import service.definitions.recommendation.{RecommendationService}
import models.application.Item
import models.user.MobileUser
import scala.concurrent.Future
import scala.util.Try
import scala.util.Success
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

class RecommendationServiceImpl extends RecommendationService {

  def recommendItemsToUser(nrItems: Int, applicationName: String, user: String): Future[List[Item]] = null

  def getSimilarItems(item: Item): Future[List[Item]] = null

}

