package service.user.implementations

import models.user.{User}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json
import scala.util.Failure
import scala.util.Try
import service.user.definitions.{UserService2}
import com.google.inject._
import service.persistence.definitions.{DatabaseService}

class UserService2Impl @Inject()(
  databaseService: DatabaseService
) extends UserService2 {

  databaseService.init("users")

  private val UserId = "email"
  private val ApplicationsField = "applications"

  def insertUser(user: User): Try[Unit] = {
    user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
    databaseService.insert(Json.toJson(user))
  }

  def find(email: String): Option[User] = {
    databaseService.get(UserId, email) match {
      case Some(user) => {
        user.validate[User].fold(
          valid = (u => Some(u)),
          invalid = (_ => None)
        )
      }
      case None => None
    }
  }

  def exists(email: String): Boolean = {
    databaseService.exists(UserId, email)
  }

  def deleteUser(user: User): Try[Unit] = {
    databaseService.delete(Json.toJson(user))
  }

  def addApplication(email: String, applicationId: String): Try[Unit] = {
    if(! exists(email)) {
      new Failure(new Exception("User does not exists"))
    } else {
      databaseService.addElementToArray[String](UserId, email, ApplicationsField, applicationId)
    }
  }

  def getApplications(email: String): List[String] = {
    this.find(email) match {
      case Some(user) => user.applications
      case None => Nil
    }
  }

  def authenticate(email: String, password: String): Option[User] = {
    this.find(email).filter {
      user => BCrypt.checkpw(password, user.password)
    }
  }
}


