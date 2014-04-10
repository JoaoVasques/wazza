package service.definitions.recommendation

import models.application.Item
import models.user.MobileUser
import scala.concurrent.Future
import scala.util.Try

trait RecommendationService {

  def addUserToRecommendationEngine(user: MobileUser): Future[Try[Unit]]

  def addItemToRecommendationEngine(item: Item): Future[Try[Unit]]

  def recommendItemsToUser(nrItems: Int, applicationName: String, user: MobileUser): Future[List[Item]]

  def getSimilarItems(item: Item): Future[List[Item]]
}

