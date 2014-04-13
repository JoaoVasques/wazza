package service.definitions.recommendation

import models.application.Item
import models.user.MobileSession
import scala.concurrent.Future
import scala.util.Try

trait RecommendationService {

  def recommendItemsToUser(nrItems: Int, applicationName: String, userId: String): Future[List[Item]]

  def getSimilarItems(item: Item): Future[List[Item]]
}

object RecommendationTypes{
  lazy val SessionType = "session"
  lazy val PurchaseType = "purchase"
}

