package service.user.implementations

import models.user._
import service.user.definitions.UserService
import com.mongodb.casbah.Imports._
import play.api.data.validation._
import org.mindrot.jbcrypt.BCrypt
import scala.util.{Try, Success, Failure}

class UserServiceImpl extends UserService {

	private val usersCollection = WazzaUser.getDAO

	def insertUser(user: WazzaUser) = {
		user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
		usersCollection.insert(user)
	}

	def findBy(attribute: String, key: String): Option[WazzaUser] = {
		usersCollection.findOne(MongoDBObject(attribute -> key))
	}

	def exists(email: String): Boolean = {
		! usersCollection.findOne(MongoDBObject("email" -> email)).isEmpty
	}

	def getApplications(email: String): List[String] = {
		val maybeUser = findBy("email", email)
		maybeUser match {
			case Some(user) => user.applications
			case None => Nil
		}
	}

	def addApplication(email: String, applicationId: String): Try[Unit] = {
		if(! exists(email)){
			new Failure(new Exception("User does not exists"))
		} else {
			usersCollection.update(
				MongoDBObject("email" -> email),
				$push("applications" -> applicationId)
			)
			new Success
		}
	}

	def authenticate(email: String, password: String): Option[WazzaUser] = {
		findBy("email", email).filter {
			user => BCrypt.checkpw(password, user.password)
		}
	}
}
