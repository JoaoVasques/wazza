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
import play.api.libs.Files._
import java.io.File
import play.api.mvc.MultipartFormData._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.implicitConversions

class ItemServiceImpl @Inject()(
	applicationService: ApplicationService
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
		language: String,
		imageName: String,
		imageUrl: String
	): Future[Try[Item]] = {

		val promise = Promise[Try[Item]]

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
			new Currency(typeOfCurrency, price, virtualCurrency),
			imageInfo = new ImageInfo(imageName, imageUrl)
		)

		promise.success(applicationService.addItem(item, applicationName))
		promise.future
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
			new Currency(RealWordCurrencyType, pricingProperties.price, null),
			new ImageInfo("","")
		)

		applicationService.addItem(item, applicationName)
	}

	def createItemFromMultipartData(formData: MultipartFormData[_], applicationName: String): Future[Try[Item]] = {
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
			case None => errors += generateJsonError("name", "Please insert a name")
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

		data get "imageInfo" match {
			case Some(info) => itemData += ("imageInfo" -> Json.parse(info.head))
			case None => errors += generateJsonError("imageInfo", "No image info")
		}
		
		if(errors.size > 0){
			val promise = Promise[Try[Item]]
			promise.failure(new Exception(JsArray(errors).toString))
			promise.future
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
				(itemData("metadata") \ "language").as[String],
				(itemData("imageInfo") \ "name").as[String],
				(itemData("imageInfo") \ "url").as[String]
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

  private implicit def extractFile(filePart: FilePart[_]): File = {
    filePart.ref match {
       case TemporaryFile(file) => file
    }
  }
}
