package service.user.definitions

import models.user._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import scala.util.{Try, Success, Failure}

trait UserService {

	def insertUser(user: WazzaUser)

	def findBy(attribute: String, key: String): Option[WazzaUser]

	def exists(email: String): Boolean

	def validateUser(email: String): Boolean = {
		! exists(email)
	}

  def addApplication(email: String, applicationId: String): Try[Unit]

  def getApplications(email: String): List[String]

  def authenticate(email: String, password: String): Option[WazzaUser]
}
