package service.definitions

import models._

trait ItemService {

	def createItem(): Unit

	def deleteItem(applicationId: String, itemId: String): Unit

	def exists(applicationId: String, itemId: String): Boolean

	def getItem(applicationId: String, itemId: String): Item
}
