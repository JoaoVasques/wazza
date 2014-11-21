package service.security.implementations

import scala.concurrent._
import play.api.libs.json._
import service.security.definitions.{RedirectionTableService}
import com.google.inject._
import service.persistence.definitions.DatabaseService
import ExecutionContext.Implicits.global

class RedirectionTableServiceImpl @Inject()(
  databaseService: DatabaseService
) extends RedirectionTableService {

  private lazy val collection = "RedirectionTable"

  def save(token: String, companyName: String, applicationName: String): Future[Unit] = {
    val model = Json.obj(
      "token" -> token,
      "companyName" -> companyName,
      "applicationName" -> applicationName
    )
    databaseService.insert(collection, model)
  }

  def getAppData(token: String): Future[Option[JsValue]] = {
    databaseService.get(collection, "token", token)
  }

  def delete(token: String): Future[Unit] = {
    getAppData(token) flatMap {res =>
      res match {
        case Some(data) => databaseService.delete(collection, data)
        case None => Future.successful()
      }
    }
  }
}

