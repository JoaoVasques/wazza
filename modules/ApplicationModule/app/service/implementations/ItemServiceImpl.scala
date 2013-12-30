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

		val metadata = new GoogleMetadata(
			"Google",
			name,
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
			name,
			description,
			GoogleStoreId,
			metadata,
			new Currency(RealWordCurrency, price)
		)

		applicationService.addItem(item, applicationName)
	}

	def createAppleItem(
		applicationName: String,
		title: String,
		name: String,
  	description: String,
  	store: Int,
  	productProperties: AppleProductProperties,
	  languageProperties: AppleLanguageProperties,
	  pricingProperties: ApplePricingProperties,
	  durationProperties: AppleDurationProperties
	): Try[Item] = {

		val metadata = new AppleMetadata(
			"Apple",
			name,
			title,
			description,
			productProperties,
			languageProperties,
			pricingProperties,
			durationProperties
		)

		val item = new Item(
			name,
			description,
			AppleStoreId,
			metadata,
			new Currency(RealWordCurrency, pricingProperties.price)
		)

		applicationService.addItem(item, applicationName)
	}
}
