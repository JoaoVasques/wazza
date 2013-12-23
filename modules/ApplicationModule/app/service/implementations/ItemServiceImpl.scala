package service.application.implementations

import service.application.definitions.ItemService
import service.application.definitions.ApplicationService
import models.application._
import com.google.inject._

class ItemServiceImpl @Inject()(applicationService: ApplicationService) extends ItemService {
	
	private val dao = Item.getDAO

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
	): Unit = {

		val metadata = new GoogleMetadata(
			id,
			title,
			description,
			publishedState,
			purchaseType,
			autoTranslate,
			List[GoogleTranslations](),
			autofill,
			language,
			price
		)

		val item = new Item(
			id,
			name,
			description,
			0,
			metadata,
			new Currency(0, price)
		)

		applicationService.addItem(item, applicationName)
	}

	def createAppleItem() = {

	}

	private def createMetadata(): Unit = {
		
	}

	def deleteItem(applicationId: String, itemId: String) = {

	}

	def exists(applicationId: String, itemId: String): Boolean = {
		true
	}

	def getItem(applicationId: String, itemId: String): Item = null

}
