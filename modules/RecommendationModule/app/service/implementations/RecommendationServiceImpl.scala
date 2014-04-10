package service.implementations.recommendation

import akka.util.Timeout
import service.definitions.recommendation.{RecommendationService}
import models.application.Item
import models.user.MobileUser
import scala.concurrent.Future
import scala.util.Try
import scala.util.Success
import actors.recommendation.PredictionIOActor
import actors.recommendation.{AddUserMessage, AddItemMessage}
import play.libs.Akka._
import akka.pattern.ask
import scala.concurrent._
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global

class PredictionIORecommendationService extends RecommendationService {

  private val actor = PredictionIOActor.get
  implicit val askTimeout = new Timeout(5000)

  def addUserToRecommendationEngine(user: MobileUser): Future[Try[Unit]] = {
    val msg = new AddUserMessage(user)
    ask(actor, msg) map { res =>
      println(s"actor result.. $res")
      Success()
    }
  }

  def addItemToRecommendationEngine(item: Item): Future[Try[Unit]] = null

  def recommendItemsToUser(nrItems: Int, applicationName: String, user: MobileUser): Future[List[Item]] = null

  def getSimilarItems(item: Item): Future[List[Item]] = null

}

