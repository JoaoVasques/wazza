package test.user

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Controller
import scala.util.Failure
import scala.util.Success
import com.google.inject._
import service.persistence.implementations.MongoDatabaseService
import service.user.implementations.{UserServiceImpl}
import models.user.{User}

class  UserServiceTest extends Specification { 

  private object UserData {
    val name = "testName"
    val email = "test@mail.com"
    val password = "root"
    val company = "company"
    val applications = List[String]()
  }

  "Basic User operations" should {
    running(FakeApplication()){

      val uri = "mongodb://localhost:27017/wazza-test"
      val mongoDBService = new MongoDatabaseService
      mongoDBService.init(uri, "users")
      val userService = new UserServiceImpl(mongoDBService)

      "Insert and Find" in {
        val user = new User(
          UserData.name,
          UserData.email,
          UserData.password,
          UserData.company,
          UserData.applications
        )
        userService.insertUser(user) must equalTo(Success())
        userService.exists(UserData.email) must equalTo(true)
      }

      "Applications operations" in {
        val applicationId = "Application 1"
        userService.addApplication(UserData.email, applicationId) must equalTo(Success())
        userService.getApplications(UserData.email) must equalTo(List(applicationId))
      }

      "User authentication" in {
        val correct = userService.authenticate(UserData.email, UserData.password) match {
          case Some(_) => true
          case None => false
        }

        val wrong = userService.authenticate(UserData.email, "wrong password") match {
          case Some(_) => false
          case None => true
        }

        mongoDBService.dropCollection()
        (correct && wrong) must equalTo(true)
      }
    }
  }
}
