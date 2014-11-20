package service.persistence.definitions

import play.api.libs.json.JsValue
import scala.concurrent._

trait KeyValueStoreService {

  def getAppData(token: String): Future[JsValue]

  def createAppData(token: String, companyName: String, applicationName: String): Future[Unit]

  def deleteAppData(token: String): Future[Unit]

}

