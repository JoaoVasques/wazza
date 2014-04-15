package service.application.implementations

import play.api.libs.json.JsObject
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import service.application.definitions._
import models.application._
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import InAppPurchaseContext._
import scala.util.{Try, Success, Failure}
import scala.reflect.runtime.universe._
import com.google.inject._
import service.aws.definitions.{PhotosService}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.implicitConversions
import service.persistence.definitions.DatabaseService
import WazzaApplicationImplicits._

class ApplicationServiceImpl @Inject()(
    photoService: PhotosService,
    databaseService: DatabaseService
) extends ApplicationService with ApplicationErrors{

  private implicit def convertItemToJsObject(item: Item): JsObject = {
    Item.convertToJson(item) match {
      case i: JsObject => i
      case _ => null //throw excpetion later on..
    }
  }

  def createFailure[A](error: String): Failure[A] = {
    new Failure(new Exception(error))
  }

  
  def getApplicationyTypes: List[String] = {
    WazzaApplication.applicationTypes
  }

  def getApplicationCountries(companyName: String, appName: String): List[String] = {
    List("PT")
  }

  def getApplicationCredentials(companyName: String, appName: String): Option[Credentials] = {
    databaseService.get(WazzaApplication.Key, appName, WazzaApplication.CredentialsId)
  }

  def insertApplication(companyName: String, application: WazzaApplication): Try[WazzaApplication] = {
    val collection = WazzaApplication.getCollection(companyName, application.name)
    if(! databaseService.exists(collection, WazzaApplication.Key, application.name)) {
      databaseService.insert(collection, application) match {
        case Success(_) => Success(application)
        case Failure(f) => Failure(f)
      }
    } else {
      createFailure[WazzaApplication](ApplicationWithNameExistsError(application.name))
    }
  }

  // TODO: drop collection
  def deleteApplication(companyName: String, application: WazzaApplication): Try[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, application.name)
    if(databaseService.exists(collection, WazzaApplication.Key, application.name)) {
      databaseService.delete(collection, application)
    } else {
      createFailure[Unit](s"Application ${application.name}  does not exists")
    }
  }

  def exists(companyName: String, name: String): Boolean = {
    find(companyName, name) match {
      case Some(_) => true
      case _ => false
    }
  }

  def find(companyName: String, name: String): Option[WazzaApplication] = {
    val collection = WazzaApplication.getCollection(companyName, name)
    databaseService.get(collection, WazzaApplication.Key, name)
  }

  def addItem(companyName: String, item: Item, applicationName: String): Try[Item] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.addElementToArray[JsObject](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.ItemsId,
      item
    ) match {
      case Success(_) => Success(item)
      case Failure(f) => Failure(f)
    }
  }

  def getItem(companyName: String, itemId: String, applicationName: String): Option[Item] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementFromArray[String](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.ItemsId,
      Item.ElementId,
      itemId
    ) match {
      case Some(i) => Some(i)
      case None => None
    }
  }

  def getItems(companyName: String, applicationName: String, offset: Int = 0, projection: String = null): List[Item] = {
    // WARNING: this is inefficient because it loads all items from DB. For now, just works... to be fixed later
    this.find(companyName, applicationName) match {
      case Some(application) => application.items.drop(offset).take(ItemBatch)
      case None => Nil
    }
  }

  def itemExists(companyName: String, itemName: String, applicationName: String): Boolean = {
    this.getItem(companyName, itemName, applicationName) match {
      case Some(_) => true
      case _ => false
    }
  }

  def deleteItem(companyName: String, itemId: String, applicationName: String, imageName: String): Future[Unit] = {
    val promise = Promise[Unit]
    val item = this.getItem(companyName, itemId, applicationName) match {
      case Some(item) => {
        val collection = WazzaApplication.getCollection(companyName, applicationName)
        databaseService.deleteElementFromArray[String](
          collection,
          WazzaApplication.Key,
          applicationName,
          WazzaApplication.ItemsId,
          Item.ElementId,
          itemId
        ) match {
          case Success(_) => {
            photoService.delete(imageName) map {res =>
              promise.success()
            } recover {
              case err: Exception => promise.failure(err)
            }
          }
          case Failure(f) => promise.failure(f)
        }
      }
      case _ => promise.failure(new Exception("Item does not exist"))
    }
    promise.future
  }

  def addVirtualCurrency(
    companyName: String,
    currency: VirtualCurrency,
    applicationName: String
  ): Try[VirtualCurrency] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.addElementToArray[JsObject](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      VirtualCurrency.buildJson(currency).as[JsObject]
    ) match {
      case Success(_) => Success(currency)
      case Failure(f) => Failure(f)
    }
  }

  //TODO
  def deleteVirtualCurrency(
    companyName: String,
    currencyName: String,
    applicationName: String
  ): Try[Unit] = {
    null
  }

  def getVirtualCurrency(
    companyName: String,
    currencyName: String,
    applicationName: String
  ): Option[VirtualCurrency] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementFromArray[String](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      VirtualCurrency.Id,
      currencyName
    )
  }

  def getVirtualCurrencies(companyName: String, applicationName: String): List[VirtualCurrency] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementsOfArray(
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      None
    ).map{el =>
      VirtualCurrency.buildFromJson(Some(el)).get
    }
  }

  def virtualCurrencyExists(companyName: String, currencyName: String, applicationName: String): Boolean = {
    false
  }
}
