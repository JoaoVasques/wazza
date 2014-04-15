package service.user.implementations

import models.user.{User}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json
import scala.util.Failure
import scala.util.Try
import service.user.definitions.{UserService}
import com.google.inject._
import service.persistence.definitions.{DatabaseService}

class UserServiceImpl @Inject()(
  databaseService: DatabaseService
) extends UserService {

  private val UserId = "email"
  private val ApplicationsField = "applications"

  def insertUser(user: User): Try[Unit] = {
    val collection = User.getCollection
    user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
    databaseService.insert(collection, Json.toJson(user))
  }

  def find(email: String): Option[User] = {
    val collection = User.getCollection()
    databaseService.get(collection, UserId, email) match {
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
    val collection = User.getCollection
    databaseService.exists(collection, UserId, email)
  }

  def deleteUser(user: User): Try[Unit] = {
    val collection = User.getCollection
    databaseService.delete(collection, Json.toJson(user))
  }

  def addApplication(email: String, applicationId: String): Try[Unit] = {
    if(! exists(email)) {
      new Failure(new Exception("User does not exists"))
    } else {
      val collection = User.getCollection
      databaseService.addElementToArray[String](collection, UserId, email, ApplicationsField, applicationId)
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


