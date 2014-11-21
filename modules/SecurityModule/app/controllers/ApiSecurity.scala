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
import service.security.modules._
import service.security.definitions._
import scala.concurrent._
import ExecutionContext.Implicits.global

private[security] class ApiRequest[A](
  val companyName: String,
  val applicationName: String,
  request: Request[A]
) extends WrappedRequest[A](request)

private[security] case class ApiAction[A](action: Action[A]) extends Action[A] {

  lazy private val redirectTableService = Guice.createInjector(new SecurityModule)
    .getInstance(classOf[RedirectionTableService])

  private val TokenHeader = ApiSecurityAction.TokenHeader
  lazy val parser = action.parser

  def apply(request: Request[A]): Future[SimpleResult] = {
    request.headers.get(TokenHeader).orElse(None) match {
      case Some(token) => {
        redirectTableService.getAppData(token.filter(_ != '"')) flatMap {res =>
          res match {
            case Some(json) => {
              val companyName = (json \ "companyName").as[String]
              val applicationName = (json \ "applicationName").as[String]
              action(new ApiRequest(companyName, applicationName, request))
            }
            case _ => Future.successful(Forbidden("SDK token does not exist"))
          }
        }
      }
      case None => Future.successful(Forbidden("Token not found in header"))
    }
  }
}

object ApiSecurityAction extends ActionBuilder[ApiRequest] {

  lazy val TokenHeader = "SDK-TOKEN"

  def invokeBlock[A](request: Request[A], block: (ApiRequest[A] => Future[SimpleResult])) = {
    request match {
      case req: ApiRequest[A] => block(req)
      case _ => Future.successful(BadRequest("Invalid Request"))
    }
  }

  override def composeAction[A](action: Action[A]) = ApiAction(action)
}

