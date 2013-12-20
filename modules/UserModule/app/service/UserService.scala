package user.service

import user.models._

trait UserService {

	def helloWorld()

	def insertUser(user: User)

	def findBy(attribute: String, key: String): List[User]

	def exists(email: String): Boolean
}
