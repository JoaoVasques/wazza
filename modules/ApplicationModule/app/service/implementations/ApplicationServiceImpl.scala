package service.application.implementations

import play.api.libs.json.JsObject
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import service.application.definitions._
import models.application._
import models.user.{CompanyData}
import play.api.libs.json.{Json, JsArray, JsValue}
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
    databaseService.get(WazzaApplication.Key, appName, WazzaApplication.CredentialsId) map {opt =>
      opt match {
        case Some(credentials) => {
          credentials.validate[Credentials].fold(
            valid = (c => Some(c)),
            invalid = (_ => None)
          )
        }
        case None => None
      }
    }
  }

  def insertApplication(companyName: String, application: WazzaApplication): Future[WazzaApplication] = {
    val collection = WazzaApplication.getCollection(companyName, application.name)
    exists(companyName, application.name) flatMap {exist =>
      if(!exist) {
        for{
          res <- databaseService.insert(collection, application)
          app <- addApplication(companyName, application.name)
        } yield application
      } else Future {null}
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
    databaseService.get(collection, WazzaApplication.Key, name) map {opt =>
      WazzaApplicationImplicits.buildOptionFromOptionJson(opt)
    }
  }

  def addItem(companyName: String, item: Item, applicationName: String): Future[Unit] = {
    val collection = WazzaApplication.getCollection(companyName, applicationName)
    databaseService.addElementToArray[JsObject](
      collection,
      WazzaApplication.Key,
      applicationName,
      WazzaApplication.ItemsId,
      Item.convertToJson(item)
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
      Item.buildFromJsonOption(optItem)
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
    getItem(companyName, itemId, applicationName) map { optItem =>
      optItem match {
        case Some(item) => {
          databaseService.deleteElementFromArray[String](
            WazzaApplication.getCollection(companyName, applicationName),
            WazzaApplication.Key,
            applicationName,
            WazzaApplication.ItemsId,
            Item.ElementId,
            itemId
          ) flatMap {r =>
            photoService.delete(imageName) map {res =>
              promise.success()
            } recover {
              case err: Exception => promise.failure(err)
            }
          }
        }
        case _ => promise.failure(new Exception("Item does not exist"))
      }
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
    val promise = Promise[Unit]
    companyExists(companyName) map {exists =>
      if(!exists) {
        val data = new CompanyData(companyName, List[String]())
        databaseService.insert(CompanyData.Collection, Json.toJson(data)) map {r =>
          promise.success()
        } recover {
          case e: Exception => promise.failure(e)
        }
      } else {
        promise.failure(new Exception("Company already exists"))
      }
    }
    promise.future
  }

  def addApplication(companyName: String, applicationName: String): Future[Unit] = {
    val promise = Promise[Unit]
    applicationExists(companyName, applicationName) map {exists =>
      if(!exists) {
        databaseService.addElementToArray[String](
          CompanyData.Collection,
          CompanyData.Key,
          companyName,
          CompanyData.Apps,
          applicationName
        ) map {r =>
          promise.success()
        } recover {
          case e: Exception => {
            println(e.getMessage)
            promise.failure(e)
          }
        }
      } else {
        println("company already exists")
        promise.failure(new Exception("Application already exists"))
      }
    }

    promise.future
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
    ) map { list =>
      (list.head \ "apps").as[List[String]].find((app: String) => {
        applicationName == app
      }) match {
        case Some(_) => true
        case None => false
      }
    }
  }
}

