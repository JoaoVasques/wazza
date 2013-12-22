package service.implementations

import service.definitions.ItemService
import models.Item

class ItemServiceImpl extends ItemService {
	
	private val dao = Item.getDAO

	def createItem() = {

	}

	def deleteItem(applicationId: String, itemId: String) = {

	}

	def exists(applicationId: String, itemId: String): Boolean = {
		true
	}

	def getItem(applicationId: String, itemId: String): Item = null

}
