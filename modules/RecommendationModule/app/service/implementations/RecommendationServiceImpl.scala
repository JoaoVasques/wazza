package service.implementations.recommendation

import play.api.Configuration
import play.api.Play
import models.user.MobileSession
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import scala.util.Failure
import service.definitions.recommendation.{RecommendationService}
import service.application.definitions.{ApplicationService}
import models.application.Item
import models.user.MobileUser
import scala.concurrent.Future
import scala.util.Try
import scala.util.Success
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws._
import com.google.inject._
import service.persistence.definitions.DatabaseService

class RecommendationServiceImpl @Inject()(
  applicationService: ApplicationService,
  databaseService: DatabaseService
) extends RecommendationService {

  private lazy val RecommendItemsToUser = "Rec-Items-User"
  private lazy val RecommendSimilarItems = "Rec-Similar-Items"

  private case class RecommendationServerInfo(
    host: String,
    port: Int,
    endpoints: Map[String, String]
  ) {
    def getEndpoint(endpointType: String, args: Map[String, String]): String = {
      endpointType match {
        case RecommendSimilarItems => {
          //TODO
          null
        }
        case RecommendItemsToUser => {
          val userId = args("userId")
          val companyName = args("companyName")
          val appName = args("appName")
          if(!args.contains("nrItems")) {
            s"${host}:${port}${endpoints(RecommendSimilarItems)}/${userId}/${companyName}/${appName}".replaceAll(" ","")
          } else {
            val limit = args("nrItems")
            s"${host}:${port}${endpoints(RecommendSimilarItems)}/${userId}/${companyName}/${appName}/${limit}".replaceAll(" ","")
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

    val args = if(nrItems > 0) {
      Map("companyName" -> companyName,
        "appName" -> applicationName,
        "userId" -> id,
        "nrItems" -> nrItems.toString
      )
    } else {
      Map("companyName" -> companyName,
        "appName" -> applicationName,
        "userId" -> id
      )
    }

    val endpoint = serverInfo.getEndpoint(RecommendItemsToUser, args)
    val promise = Promise[JsArray]
    WS.url(endpoint).get() map { result =>
      try {
        result.status match {
          case 200 => {
            val arr = Json.parse(result.body).as[JsArray]
            println(s"recommended results $arr")
            if(arr.value.isEmpty) {
              // Get items that the user has not bought yet
              val x = applicationService.getItemsNotPurchased(companyName, applicationName, userId, nrItems)
              println(s"xxx $x")
              val items = applicationService.getItems(companyName, applicationName, 0).map { el =>
                Item.convertToJson(el)
              }.toSeq
              promise.success(new JsArray(items))
            } else {
              promise.success(arr)
            }
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

