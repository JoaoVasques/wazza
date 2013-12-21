package service.user.implementations

import models.user._
import service.user.definitions.UserService
import com.mongodb.casbah.Imports._
import play.api.data.validation._

class UserServiceImpl extends UserService {

	private val usersCollection = User.getDAO

	def insertUser(user: User) = {
		usersCollection.insert(user)
	}

	def findBy(attribute: String, key: String): List[User] = {
		usersCollection.find(MongoDBObject(attribute -> key)).toList
	}

	def exists(email: String): Boolean = {
		usersCollection.findOne(MongoDBObject("email" -> email)).isEmpty
	}
}
