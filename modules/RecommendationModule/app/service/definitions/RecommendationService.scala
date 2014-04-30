package service.definitions.recommendation

import models.application.Item
import models.user.MobileSession
import play.api.libs.json.JsArray
import scala.concurrent.Future
import scala.util.Try

trait RecommendationService {

  def recommendItemsToUser(
    companyName: String,
    applicationName: String,
    userId: String,
    nrItems: Int
  ): Future[JsArray]

  def getSimilarItems(item: Item): Future[List[Item]]
}

object RecommendationTypes{
  lazy val SessionType = "session"
  lazy val PurchaseType = "purchase"
}

