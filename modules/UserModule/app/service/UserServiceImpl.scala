package user.service

import user.models._
import com.mongodb.casbah.Imports._

class UserServiceImpl extends UserService {

	val usersCollection = User.getDAO

	def helloWorld() = {
		println("hello")
	}

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