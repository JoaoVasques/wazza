package service.user.implementations

import models.user._
import service.user.definitions.UserService
import com.mongodb.casbah.Imports._
import play.api.data.validation._
import org.mindrot.jbcrypt.BCrypt

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

	def authenticate(email: String, password: String): Option[WazzaUser] = {
		findBy("email", email).filter {
			user => BCrypt.checkpw(password, user.password)
		}
	}
}
