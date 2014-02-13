package test.user

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.mvc.Controller
import scala.util.Failure
import scala.util.Success
import com.google.inject._
import service.persistence.implementations.MongoDatabaseService
import service.user.implementations.UserService2Impl
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
      val userService = new UserService2Impl(mongoDBService)

      "Insert and Find" in {
        val user = new User(
          UserData.name,
          UserData.email,
          UserData.password,
          UserData.company,
          UserData.applications
        )

        val res = userService.insertUser(user) match {
          case Success(_) => true
          case Failure(_) => false
        }

        val exists = userService.exists(UserData.email)

        ( res && exists)  must equalTo(true)
      }

      "Applications operations" in {

        val applicationId = "Application 1"
        val res = userService.addApplication(UserData.email, applicationId) match {
          case Success(_) => true
          case Failure(_) => false
        }

        val exists = userService.getApplications(UserData.email).contains(applicationId)
        (res && exists) must equalTo(true)
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
