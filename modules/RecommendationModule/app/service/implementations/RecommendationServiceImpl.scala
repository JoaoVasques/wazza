package service.implementations.recommendation

import play.api.Configuration
import play.api.Play
import models.user.MobileSession
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import scala.util.Failure
import service.definitions.recommendation.{RecommendationService}
import models.application.Item
import models.user.MobileUser
import scala.concurrent.Future
import scala.util.Try
import scala.util.Success
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws._

class RecommendationServiceImpl extends RecommendationService {

  private lazy val RecommendItemsToUser = "Rec-Items-User"
  private lazy val RecommendSimilarItems = "Rec-Similar-Items"

  private case class RecommendationServerInfo(
    host: String,
    port: Int,
    endpoints: Map[String, String]
  ) {
    def getEndpoint(endpointType: String, args: List[String]): String = {
      endpointType match {
        case RecommendSimilarItems => {
          //TODO
          null
        }
        case RecommendItemsToUser => {
          args.length match {
            case 2 => {
              val userId = args.head
              val companyName = args.last
              s"${host}:${port}${endpoints(RecommendSimilarItems)}/${companyName}/${userId}".replaceAll(" ","")
            }
            case 3 => {
              val userId = args.head
              val companyName = args(1)
              val limit = args.last.toInt
              s"${host}:${port}{endpoints(RecommendSimilarItems)}/${companyName}/{userId}/{limit}".replaceAll(" ","")
            }
            case _ => {
              //error
              null
            }
          }       
        }
      }
    }
  }

  private val serverInfo: RecommendationServerInfo = initServerInfo

  private def initServerInfo(): RecommendationServerInfo = {

    def getConf(config: Configuration, key: String) = {
      config.underlying.root.get(key).render.filter(_ != '"')
    }

    Play.current.configuration.getConfig("recommendation") match {
      case Some(conf) => {
        val url = getConf(conf, "url")
        val port = getConf(conf, "port").toInt
        val endpoints = Map(
          RecommendItemsToUser -> "/rec/user/items",
          RecommendSimilarItems -> " /rec/user/items"
        )
        new RecommendationServerInfo(url, port, endpoints)
      }
      case None => null
    }
  }

  def recommendItemsToUser(
    companyName: String,
    applicationName: String,
    userId: String,
    nrItems: Int
  ): Future[JsArray] = {

    val args = if(nrItems > 0) {
      List(applicationName, userId, nrItems.toString)
    } else {
      List(applicationName, userId)
    }

    val endpoint = serverInfo.getEndpoint(RecommendItemsToUser, args)
    val promise = Promise[JsArray]
    WS.url(endpoint).get() map { result =>
      try {
        result.status match {
          case 200 => {
            val arr = Json.parse(result.body).as[JsArray]
            promise.success(arr)
          }
          case _ => {
            promise.failure(new Exception(result.ahcResponse.getResponseBody()))
          }
        }
        
      } catch {
        case e: Exception => promise.failure(e)
      }
    } recover {
      case e: Exception => promise.failure(e)
    }
    promise.future
  }

  def getSimilarItems(item: Item): Future[List[Item]] = {
    null
  }
}

