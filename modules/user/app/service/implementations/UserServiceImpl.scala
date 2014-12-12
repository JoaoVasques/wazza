package service.user.implementations

import models.user.{User, CompanyData}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json
import scala.util.Failure
import scala.util.Try
import service.user.definitions.{UserService}
import com.google.inject._
import service.persistence.definitions.{DatabaseService}
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

class UserServiceImpl @Inject()(
  databaseService: DatabaseService
) extends UserService {

  private val UserId = "email"
  private val ApplicationsField = "applications"

  def insertUser(user: User): Future[Unit] = {
    val collection = User.getCollection
    user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
    databaseService.insert(collection, Json.toJson(user)) flatMap {res =>
      val companyData = new CompanyData(user.company, List[String]())
      databaseService.insert(CompanyData.Collection, Json.toJson(companyData))
    }
  }

  def find(email: String): Future[Option[User]] = {
    val collection = User.getCollection()
    databaseService.get(collection, UserId, email) map {opt =>
      opt match {
        case Some(user) => {
          user.validate[User].fold(
            valid = (u => Some(u)),
            invalid = (_ => None)
          )
        }
        case None => None
      }
    }
  }

  def exists(email: String): Future[Boolean] = {
    val collection = User.getCollection
    databaseService.exists(collection, UserId, email)
  }

  def deleteUser(user: User): Future[Unit] = {
    val collection = User.getCollection
    databaseService.delete(collection, Json.toJson(user))
  }

  def addApplication(email: String, applicationId: String): Future[Unit] = {
    exists(email) flatMap {exist =>
      if(!exist) {
        Future { new Exception("User does not exists") }
      } else {
        val collection = User.getCollection
        databaseService.addElementToArray[String](collection, UserId, email, ApplicationsField, applicationId) 
      }
    }
  }

  def getApplications(email: String): Future[List[String]] = {
    this.find(email) map {opt =>
      opt match {
        case Some(user) => user.applications
        case None => Nil
      }
    }
  }

  def authenticate(email: String, password: String): Option[User] = {
    import user.{UserProxy}
    import user.messages._
    import scala.collection.mutable.Stack
    import akka.util.{Timeout}
    import akka.pattern.ask
    import user.messages.URAuthenticationResponse

    val request = new URAuthenticate(new Stack(), email, password, true)
    implicit val timeout = Timeout(10 seconds)
    val futureAuth = (UserProxy.getInstance ? request)
    Await.result(futureAuth, timeout.duration).asInstanceOf[URAuthenticationResponse].res
  }
}

