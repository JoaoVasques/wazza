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
import service.user.definitions.PurchaseService
import models.user.PurchaseInfo

class ApplicationServiceImpl @Inject()(
    photoService: PhotosService,
    databaseService: DatabaseService,
    purchaseService: PurchaseService
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

  def getApplicationCredentials(companyName: String, appName: String): Future[Option[Credentials]] = {
    databaseService.get(WazzaApplication.Key, appName, WazzaApplication.CredentialsId)
  }

  def insertApplication(companyName: String, application: WazzaApplication): Future[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, application.name)

    exists(companyName, application.name) flatMap {exist =>
      if(!exist) {
        databaseService.insert(collection, application) flatMap {app =>
          addApplication(companyName, application.name)
        }
      } else {
        Future {new Exception("Application already exists")}
      }
    }
  }

  // TODO: drop collection
  def deleteApplication(companyName: String, application: WazzaApplication): Future[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, application.name)
    exists(companyName, application.name) flatMap {exist =>
      if(exist) {
        databaseService.delete(collection, application)
      } else {
        Future {new Exception(s"Application ${application.name}  does not exists") }
      }
    }
  }

  def exists(companyName: String, name: String): Future[Boolean] = {
    find(companyName, name) map {opt =>
      opt match {
        case Some(_) => true
        case _ => false
      }
    }
  }

  def find(companyName: String, name: String): Future[Option[WazzaApplication]] = {
    val collection = WazzaApplication.getCollection(companyName, name)
    databaseService.get(collection, WazzaApplication.Key, name)
  }

  def addItem(companyName: String, item: Item, applicationName: String): Future[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.addElementToArray[JsObject](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.ItemsId,
      item
    )
  }

  def getItem(companyName: String, itemId: String, applicationName: String): Future[Option[Item]] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementFromArray[String](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.ItemsId,
      Item.ElementId,
      itemId
    ) map {optItem =>
      optItem match {
        case Some(i) => Some(i)
        case None => None
      }
    }
  }

  def getItems(
    companyName: String,
    applicationName: String,
    offset: Int = 0,
    projection: String = null
  ): Future[List[Item]] = {
    // WARNING: this is inefficient because it loads all items from DB. For now, just works... to be fixed later
    this.find(companyName, applicationName) map {appOpt =>
      appOpt match {
        case Some(application) => application.items.drop(offset).take(ItemBatch)
        case None => Nil
      }
    }
  }

  def getItemsNotPurchased(
    companyName: String,
    applicationName: String,
    userId: String,
    limit: Int
  ): Future[List[Item]] = {
    purchaseService.getUserPurchases(companyName, applicationName, userId) flatMap {p =>
      val purchases = p map {(p: PurchaseInfo) =>p.itemId }
      databaseService.getElementsWithoutArrayContent(
        WazzaApplication.getCollection(companyName, applicationName),
        WazzaApplication.ItemsId,
        Item.ElementId,
        purchases,
        limit
      ) map {els =>
        els map {i => Item.buildFromJson(i)}
      }
    }
  }

  def itemExists(companyName: String, itemName: String, applicationName: String): Future[Boolean] = {
    this.getItem(companyName, itemName, applicationName) map {opt =>
      opt match {
        case Some(_) => true
        case _ => false
      }
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
  ): Future[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.addElementToArray[JsObject](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      VirtualCurrency.buildJson(currency).as[JsObject]
    )
  }

  //TODO
  def deleteVirtualCurrency(
    companyName: String,
    currencyName: String,
    applicationName: String
  ): Future[Unit] = {
    null
  }

  def getVirtualCurrency(
    companyName: String,
    currencyName: String,
    applicationName: String
  ): Future[Option[VirtualCurrency]] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementFromArray[String](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      VirtualCurrency.Id,
      currencyName
    )
    null
  }

  def getVirtualCurrencies(companyName: String, applicationName: String): Future[List[VirtualCurrency]] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.getElementsOfArray(
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.VirtualCurrenciesId,
      None
    ) map {vc =>
      vc map{el =>
        VirtualCurrency.buildFromJson(Some(el)).get
      }
    }
  }

  def virtualCurrencyExists(companyName: String, currencyName: String, applicationName: String): Future[Boolean] = {
    Future {false}
  }

  /**
    Private collection with information about all companies and apps
  **/
  def addCompany(companyName: String): Future[Unit] = {
    companyExists(companyName) flatMap {exists =>
      if(!exists) {
        val data = new CompanyData(companyName, List[String]())
        databaseService.insert(CompanyData.Collection, Json.toJson(data))
      }
    }
  }

  def addApplication(companyName: String, applicationName: String): Future[Unit] = {
    applicationExists(companyName, applicationName) flatMap {exists =>
      if(!exists) {
        databaseService.addElementToArray[String](
          CompanyData.Collection,
          CompanyData.Key,
          companyName,
          CompanyData.Apps,
          applicationName
        )
      }
    }
  }

  def getCompanies(): Future[List[CompanyData]] = {
    databaseService.getCollectionElements(CompanyData.Collection) map {companies =>
      companies map {el =>
        new CompanyData(
          (el \ "name").as[String],
          (el \ "apps").as[List[String]]
        )
      }
    }
  }

  private def companyExists(companyName: String): Future[Boolean] = {
    databaseService.exists(CompanyData.Collection, CompanyData.Key, companyName)
  }

  private def applicationExists(companyName: String, applicationName: String): Future[Boolean] = {
    databaseService.getElementsOfArray(
      CompanyData.Collection,
      CompanyData.Key,
      companyName,
      CompanyData.Apps,
      None
    ).toList.find((app: JsValue) => {
      //TODO
      println("app : " + app)
      true
    }) match {
      case Some(_) => true
      case None => false
    }
  }
}

