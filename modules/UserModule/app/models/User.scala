package user.models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._

case class User(
 id: ObjectId = new ObjectId,
 @Key("email_id") email: String,
 password: String,
 company: String
)

object User extends ModelCompanion[User, ObjectId] {
	val dao = new SalatDAO[User, ObjectId](collection = mongoCollection("users")) {}

	def insertUser(user: User) = {
		dao.insert(user)
	}

	def findBy(attribute: String, key: String): List[User] = {
		dao.find(MongoDBObject(attribute -> key)).toList
	}

	def exists(email: String): Boolean = {
		dao.findOne(MongoDBObject("email" -> email)).isEmpty
	}
}
