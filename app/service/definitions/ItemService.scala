package service.definitions

import models._

trait ItemService {

	def createGooglePlayItem(
		applicationName: String,
		id: String,
		title: String,
		name: String,
		description: String,
		typeOfCurrency: Int,
		price: Double,
		publishedState: String,
		purchaseType: Int,
		autoTranslate: Boolean,
		autofill: Boolean,
		language: String
	): Unit

	def createAppleItem(): Unit

	def deleteItem(applicationId: String, itemId: String): Unit

	def exists(applicationId: String, itemId: String): Boolean

	def getItem(applicationId: String, itemId: String): Item
}
