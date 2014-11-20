package service.persistence.implementations

import service.persistence.definitions.{KeyValueStoreService}
import play.api.libs.json.JsValue
import scala.concurrent._

class RedisService extends KeyValueStoreService {

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

