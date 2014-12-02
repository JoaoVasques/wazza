package service.user.definitions

import models.user.{User}
import scala.concurrent._
import ExecutionContext.Implicits.global

trait UserService {

  def insertUser(user: User): Future[Unit]

  def find(email: String): Future[Option[User]]

  def exists(email: String): Future[Boolean]

  def deleteUser(user: User): Future[Unit]

  def validateUser(email: String): Future[Boolean] = {
    this.exists(email) map { exist => !exist}
  }

  def addApplication(email: String, applicationId: String): Future[Unit]

  def getApplications(email: String): Future[List[String]]

  def authenticate(email: String, password: String): Option[User]

}
