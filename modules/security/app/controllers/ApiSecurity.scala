package controllers.security


import play.api._
import play.api.mvc._
import play.api.cache._
import play.api.libs.json._
import play.api.data._
import play.api.mvc.Results._
import play.api.mvc.BodyParsers.parse
import com.github.nscala_time.time.Imports._
import java.security.SecureRandom
import scala.annotation.tailrec
import com.google.inject._
import service.security.implementations._
import service.persistence.implementations._
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.Logger
import service.persistence.definitions._
import service.persistence.modules.PersistenceModule

private[security] class ApiRequest[A](
  val companyName: String,
  val applicationName: String,
  request: Request[A]
) extends WrappedRequest[A](request)

private[security] case class ApiAction[A](action: Action[A]) extends Action[A] {

  private val inj = Guice.createInjector(new PersistenceModule)
  lazy private val databaseService = inj.getInstance(classOf[DatabaseService])

  private lazy val collection = "RedirectionTable"

  private def saveAppData(token: String, companyName: String, applicationName: String): Future[Unit] = {
    val model = Json.obj(
      "token" -> token,
      "companyName" -> companyName,
      "applicationName" -> applicationName
    )
    databaseService.insert(collection, model)
  }

  private def getAppData(token: String): Future[Option[JsValue]] = {
    databaseService.get(collection, "token", token)
  }

  private def deleteAppData(token: String): Future[Unit] = {
    getAppData(token) flatMap {res =>
      res match {
        case Some(data) => databaseService.delete(collection, data)
        case None => Future.successful()
      }
    }
  }


  private val TokenHeader = ApiSecurityAction.TokenHeader
  lazy val parser = action.parser

  def apply(request: Request[A]): Future[Result] = {
    request.headers.get(TokenHeader).orElse(None) match {
      case Some(token) => {
        this.getAppData(token.filter(_ != '"')) flatMap {res =>
          res match {
            case Some(json) => {
              val companyName = (json \ "companyName").as[String]
              val applicationName = (json \ "applicationName").as[String]
              action(new ApiRequest(companyName, applicationName, request))
            }
            case _ => {
              Logger.info("HTTP Request error: SDK token does not exist")
              Future.successful(Forbidden("SDK token does not exist"))
            }
          }
        }
      }
      case None => {
        Logger.info("HTTP Request error: Token not found in header")
        Future.successful(Forbidden("Token not found in header"))
      }
    }
  }
}

object ApiSecurityAction extends ActionBuilder[ApiRequest] {

  lazy val TokenHeader = "SDK-TOKEN"

  def invokeBlock[A](request: Request[A], block: (ApiRequest[A] => Future[Result])) = {
    request match {
      case req: ApiRequest[A] => block(req)
      case _ => Future.successful(BadRequest("Invalid Request"))
    }
  }

  override def composeAction[A](action: Action[A]) = ApiAction(action)
}

