package persistence.plugin.actors

import play.api.libs.json.JsValue
import scala.concurrent._

protected[plugin] trait KeyValueStoreActor {

  def getAppData(token: String): Future[JsValue]

  def createAppData(token: String, companyName: String, applicationName: String): Future[Unit]

  def deleteAppData(token: String): Future[Unit]
}

