package service.application.definitions

import models.application._
import scala.util.{Try}
import play.api.mvc.{MultipartFormData}
import java.io.File
import scala.concurrent._

trait ItemService {

	def createGooglePlayItem(
		applicationName: String,
		name: String,
		description: String,
		typeOfCurrency: Int,
		virtualCurrency: Option[String],
		price: Double,
		publishedState: String,
		purchaseType: String,
		autoTranslate: Boolean,
		autofill: Boolean,
		language: String,
		imageName: String,
		imageUrl: String
	): Future[Try[Item]]

	def createAppleItem(
		applicationName: String,
		title: String,
		name: String,
		itemId: String,
  	description: String,
  	store: Int,
  	productProperties: AppleProductProperties,
	  languageProperties: AppleLanguageProperties,
	  pricingProperties: ApplePricingProperties,
	  durationProperties: AppleDurationProperties
	): Try[Item]

	def createItemFromMultipartData(data: MultipartFormData[_], applicationName: String): Future[Try[Item]]

	def getCurrencyTypes(): Map[String, Int]

	def generateMetadataFile(item: Item): File

	protected def generateId(idType: Int, name: String, purchaseType: String): String
		
}
