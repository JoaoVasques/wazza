package persistence.plugin.actors

import play.api.libs.json.JsValue
import scala.concurrent._

protected[plugin] class RedisActor extends KeyValueStoreActor {

  def getAppData(token: String): Future[JsValue] = {
    null
  }

  def createAppData(token: String, companyName: String, applicationName: String): Future[Unit] = {
    null
  }

  def deleteAppData(token: String): Future[Unit] = {
    null
  }
}
