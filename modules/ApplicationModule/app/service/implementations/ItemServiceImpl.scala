package service.application.implementations

import service.application.definitions.ItemService
import service.application.definitions.ApplicationService
import models.application._
import com.google.inject._
import scala.util.{Try, Success, Failure}
import ItemContext._

class ItemServiceImpl @Inject()(applicationService: ApplicationService) extends ItemService {
	
	private val dao = Item.getDAO

	def createGooglePlayItem(
		applicationName: String,
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
	): Try[Item] = {

		val trans1 = new GoogleTranslations("locale1", "title1", "description1")
		val trans2 = new GoogleTranslations("locale2", "title2", "description2")
		val metadata = new GoogleMetadata(
			"Google",
			name,
			title,
			description,
			publishedState,
			purchaseType,
			autoTranslate,
			List[GoogleTranslations](trans1, trans2),
			autofill,
			language,
			price
		)

		val item = new Item(
			name,
			description,
			GoogleStoreId,
			metadata,
			new Currency(0, price)
		)

		applicationService.addItem(item, applicationName)
	}

	def createAppleItem(): Try[Item] = {
		//TODO
		null
	}

}
