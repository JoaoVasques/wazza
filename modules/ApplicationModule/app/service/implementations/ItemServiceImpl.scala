package service.application.implementations

import service.application.definitions.ItemService
import service.application.definitions.ApplicationService
import models.application._
import com.google.inject._
import scala.util.{Try, Success, Failure}
import ItemContext._
import play.api.mvc.{MultipartFormData}
import play.api.libs.json._
import scala.collection.mutable.ListBuffer
import com.github.nscala_time.time.Imports._

class ItemServiceImpl @Inject()(applicationService: ApplicationService) extends ItemService {
	
	private val dao = Item.getDAO

	def createGooglePlayItem(
		applicationName: String,
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
			generateId(GoogleStoreId, name, purchaseType),
			name,
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
		itemId: String,
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

	def createItemFromMultipartData(formData: MultipartFormData[_], applicationName: String): Try[Item] = {

		var errors = ListBuffer[JsValue]()
		val data = formData.dataParts

		var itemData = Map[String,JsValue]()

		data get "name" match {
			case Some(name) => {
				if(applicationService.itemExists(name.head, "application name")){
					errors += Json.obj("name" -> "An item alreayd exists with this name")
				}
			}
			case None => errors += Json.obj("name" -> "Please insert a name")
		}

		data get "metadata" match {
			case Some(m) => itemData += ("metadata" -> Json.parse(m.head))
			case None => errors += Json.obj("metadata" -> "Missing metadata information")
		}

		data get "currency" match {
			case Some(currency) => {
				val value = (Json.parse(currency.head) \ "value").as[Double]
				if(value <= 0.0){
					errors += Json.obj("currency" -> "Price must be greater than zero")
				} else {
					itemData += ("currency" -> Json.parse(currency.head))
				}
			}
			case None => errors += Json.obj("currency" -> "Missing currency information")
		}

		if(errors.size > 0){
			Failure(new Exception(JsArray(errors).toString))
		} else {
			createGooglePlayItem(
				applicationName,
				(data get "name").get.head,
				(data get "description").get.head,
				(getCurrencyTypes get (itemData("currency") \ "typeOf").as[String]).get,
				(itemData("currency") \ "value").as[Double],
				(itemData("metadata") \ "publishedState").as[String],
				(itemData("metadata") \ "purchaseType").as[Int],
				(itemData("metadata") \ "autoTranslate").as[Boolean],
				(itemData("metadata") \ "autofill").as[Boolean],
				(itemData("metadata") \ "language").as[String]
			)
		}
	}

	def validateId(id: String): Boolean = true //TODO

	def getCurrencyTypes(): Map[String, Int] = {
		Map("Real" -> RealWordCurrency, "Virtual" -> VirtualCurrency)
	}

	protected def generateId(idType: Int, name: String, purchaseType: Int): String = {
		val date = DateTime.now.toString().replace(":","_")
		val formatedName = name.toLowerCase.replace(" ", "_")

		idType match {
			case GoogleStoreId => s"$date.$formatedName.$purchaseType"
			case AppleStoreId => null //TODO
			case _ => null
		}
	}
}
