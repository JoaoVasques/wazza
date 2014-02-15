package service.user.definitions

import scala.util.Try
import models.user.{User}

trait UserService {

  def insertUser(user: User): Try[Unit]

  def find(email: String): Option[User]

  def exists(email: String): Boolean

  def deleteUser(user: User): Try[Unit]

  def validateUser(email: String): Boolean = {
    ! this.exists(email)
  }

  def addApplication(email: String, applicationId: String): Try[Unit]

  def getApplications(email: String): List[String]

  def authenticate(email: String, password: String): Option[User]

}
