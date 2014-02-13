package controllers.persistence

import com.mongodb.casbah.commons.MongoDBList
import play.api._
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.mvc._
import scala.collection.immutable.Nil
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import com.google.inject._
import service.persistence.definitions.DatabaseService

class TestController @Inject()(
  databaseService: DatabaseService
) extends Controller {

  def test = Action {
    databaseService.hello()
    databaseService.init(databaseService.UserCollection)
    val user = Json.obj("email" -> "test@gmail.com", "name" -> "test", "applications" -> List[JsObject]())
    println(databaseService.get("email", "joao@usewazza.com"))
    databaseService.insert(user)
    databaseService.update("email", "test@gmail.com", "name", "changedName")
    println(databaseService.get("email", "test@gmail.com"))
    databaseService.addElementToArray[JsObject]("email", "test@gmail.com", "applications", Json.obj("x" -> "app test"))
    //databaseService.delete(user)
    Ok
  }

  /**
    TODO
    **/
}
