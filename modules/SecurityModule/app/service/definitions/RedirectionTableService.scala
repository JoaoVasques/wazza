package service.security.definitions

import scala.concurrent._
import play.api.libs.json._

trait RedirectionTableService {

  def save(token: String, companyName: String, applicationName: String): Future[Unit]

  def getAppData(token: String): Future[Option[JsValue]]

  def delete(token: String): Future[Unit]
}

