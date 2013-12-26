package service.application.definitions

import models.application._
import scala.util.{Try}

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
	): Try[Item]

	def createAppleItem(): Try[Item]	
}
