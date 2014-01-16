package service.application.implementations

import service.application.definitions.ItemService
import service.application.definitions.ApplicationService
import models.application._
import com.google.inject._
import scala.util.{Try, Success, Failure}
import InAppPurchaseContext._
import play.api.mvc.{MultipartFormData}
import play.api.libs.json._
import scala.collection.mutable.ListBuffer
import com.github.nscala_time.time.Imports._
import service.aws.definitions._

class ItemServiceImpl @Inject()(
	applicationService: ApplicationService,
	uploadFileService: UploadFileService
) extends ItemService {
	
	private val dao = Item.getDAO

	def createGooglePlayItem(
		applicationName: String,
		name: String,
		description: String,
		typeOfCurrency: Int,
		virtualCurrency: Option[String],
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
			new Currency(typeOfCurrency, price, virtualCurrency)
		)

		applicationService.addItem(item, applicationName)
	}

	//TODO
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
			new Currency(RealWordCurrencyType, pricingProperties.price, null)
		)

		applicationService.addItem(item, applicationName)
	}

	def createItemFromMultipartData(formData: MultipartFormData[_], applicationName: String): Try[Item] = {

		def generateJsonError(key: String, message: String): JsValue = {
			Json.obj(key -> Json.obj("message" -> message, "visible" -> true))
		}

		var errors = ListBuffer[JsValue]()
		val data = formData.dataParts
		var itemData = Map[String,JsValue]()

		data get "name" match {
			case Some(name) => {
				if(applicationService.itemExists(name.head, "application name")){
					errors += generateJsonError("name", "An item already exists with this name")
				}
			}
			case None => errors += Json.obj("name" -> "Please insert a name")
		}

		data get "metadata" match {
			case Some(m) => itemData += ("metadata" -> Json.parse(m.head))
			case None => errors += generateJsonError("metadata", "Missing metadata information")
		}

		data get "currency" match {
			case Some(currency) => {
				val value = (Json.parse(currency.head) \ "value").as[Double]
				if(value <= 0.0){
					errors += generateJsonError("currency", "Price must be greater than zero")
				} else {
					itemData += ("currency" -> Json.parse(currency.head))
				}
			}
			case None => errors += generateJsonError("currency", "Missing currency information")
		}

		if(errors.size > 0){
			Failure(new Exception(JsArray(errors).toString))
		} else {
			createGooglePlayItem(
				applicationName,
				(data get "name").get.head,
				(data get "description").get.head,
				(getCurrencyTypes get (itemData("currency") \ "typeOf").as[String]).get,
				(itemData("currency") \ "virtualCurrency").asOpt[String],
				(itemData("currency") \ "value").as[Double],
				(itemData("metadata") \ "publishedState").as[String],
				(itemData("metadata") \ "purchaseType").as[Int],
				(itemData("metadata") \ "autoTranslate").as[Boolean],
				(itemData("metadata") \ "autofill").as[Boolean],
				(itemData("metadata") \ "language").as[String]
			)
		}
	}

	def getCurrencyTypes(): Map[String, Int] = {
		Map("Real" -> RealWordCurrencyType, "Virtual" -> VirtualCurrencyType)
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
