package test.persistence

import org.specs2.mutable._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import com.google.inject._
import scala.util.Failure
import scala.util.Success
import service.persistence.definitions.DatabaseService
import service.persistence.modules.PersistenceModule

class MongoDatabaseServiceTest  extends Specification {

  private val databaseService = Guice.createInjector(new PersistenceModule).getInstance(classOf[DatabaseService])
  private val uri = "mongodb://localhost:27017/wazza-test"

  "MongoDB Basic operations" should {
    running(FakeApplication()) {
      databaseService.init(uri, "persistence")
      val user = Json.obj("email" -> "test@gmail.com", "name" -> "test", "applications" -> List[JsObject]())

      "Insert" in  {
        databaseService.insert(user) must equalTo(Success())
      }

      "Delete" in  {
        databaseService.delete(user) must equalTo(Success())
      }
      
      "Update" in  {
        databaseService.insert(user)
        databaseService.update("email", "test@gmail.com", "name", "changedName")
        val u = databaseService.get("email", "test@gmail.com")
        databaseService.delete(user)
        (u.get \ "name").as[String]  must equalTo("changedName")
      }
    }
  }

  "MongoDB Array operations" should {
    running(FakeApplication()) {
      val user = Json.obj("email" -> "test@gmail.com", "name" -> "test", "applications" -> List[JsObject]())
      val el = Json.obj("name" -> "app test")

      "Insert" in {
        databaseService.addElementToArray[JsObject](
          "email",
          "test@gmail.com",
          "applications",
          el
        ) must equalTo(Success())
      }

      "Update" in {
        val res = databaseService.updateElementOnArray[String](
         "email",
          "test@gmail.com",
          "applications",
          "name",
          "app test",
          "app test 3"
        ) match {
          case Success(_) => true
          case Failure(_) => false
        }

        res must equalTo(true)
      }

      "Delete" in {
        val res = databaseService.deleteElementFromArray[JsObject](
          "email",
          "test@gmail.com",
          "applications",
          el
        ) match {
          case Success(_) => true
          case Failure(_) => false
        }

        val element = databaseService.getElementFromArray[JsObject](
          "email",
          "test@gmail.com",
          "applications",
          "name",
          el
        )

        databaseService.dropCollection()
        res must equalTo(true)
      }
    }
  }
}
