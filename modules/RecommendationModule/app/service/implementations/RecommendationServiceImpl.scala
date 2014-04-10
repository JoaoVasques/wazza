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
import actors.recommendation.PredictionIOActor
import actors.recommendation.{
  AddUserMessage,
  AddItemMessage,
  AddSessionMessage,
  ResponseMessage,
  ErrorMessage,
  PredictionMessages
}
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
      res match {
        case ResponseMessage(s) => Success()
        case ErrorMessage(e) => Failure(new Exception(e))
      }
    }
  }

  def addUserSession(session: MobileSession): Future[Try[Unit]] = {
    val msg = new AddSessionMessage(session)
    ask(actor, msg) map { res =>
      println(s"actor result.. $res")
      res match {
        case ResponseMessage(s) => Success()
        case ErrorMessage(e) => Failure(new Exception(e))
      }
    }
  }

  def addItemToRecommendationEngine(item: Item): Future[Try[Unit]] = null

  def recommendItemsToUser(nrItems: Int, applicationName: String, user: MobileUser): Future[List[Item]] = null

  def getSimilarItems(item: Item): Future[List[Item]] = null

}

