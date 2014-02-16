package service.application.implementations

import play.api.libs.json.JsObject
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

    databaseService.init(databaseService.ApplicationCollection)

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

   def getApplicationCountries(appName: String): List[String] = {
     List("PT")
   }

    def insertApplication(application: WazzaApplication): Try[WazzaApplication] = {
      if(! databaseService.exists(WazzaApplication.Key, application.name)) {
        databaseService.insert(application) match {
          case Success(_) => Success(application)
          case Failure(f) => Failure(f)
        }
      } else {
        createFailure[WazzaApplication](ApplicationWithNameExistsError(application.name))
      }
    }

    def deleteApplication(application: WazzaApplication): Try[Unit] = {
      if(databaseService.exists(WazzaApplication.Key, application.name)) {
        databaseService.delete(application)
      } else {
        createFailure[Unit](s"Application ${application.name}  does not exists")
      }
    }

    def exists(name: String): Boolean = {
      find(name) match {
        case Some(_) => true
        case _ => false
      }
    }

    def find(name: String): Option[WazzaApplication] = {
      databaseService.get(WazzaApplication.Key, name)
    }

    def addItem(item: Item, applicationName: String): Try[Item] = {
      databaseService.addElementToArray[JsObject](
        WazzaApplication.Key,
        applicationName,
        WazzaApplication.ItemsId,
        item
      ) match {
        case Success(_) => Success(item)
        case Failure(f) => Failure(f)
      }
    }

    def getItem(itemId: String, applicationName: String): Option[Item] = {
      databaseService.getElementFromArray[String](
        WazzaApplication.Key,
        applicationName,
        WazzaApplication.ItemsId,
        itemId
      ) match {
        case Some(i) => Some(i)
        case None => None
      }
    }

    def getItems(applicationName: String, offset: Int = 0): List[Item] = {
        // WARNING: this is inefficient because it loads all items from DB. For now, just works... to be fixed later
        this.find(applicationName) match {
            case Some(application) => application.items.drop(offset).take(ItemBatch)
            case None => Nil
        }
    }

    def itemExists(itemName: String, applicationName: String): Boolean = {
      this.getItem(itemName, applicationName) match {
        case Some(_) => true
        case _ => false
      }
    }

    def deleteItem(itemId: String, applicationName: String, imageName: String): Future[Unit] = {
      val promise = Promise[Unit]
      val item = this.getItem(itemId, applicationName) match {
        case Some(item) => {
          databaseService.deleteElementFromArray(
            WazzaApplication.Key,
            applicationName,
            WazzaApplication.ItemsId,
            item
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

    def addVirtualCurrency(currency: VirtualCurrency, applicationName: String): Try[VirtualCurrency] = {
      databaseService.addElementToArray[JsObject](
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
    def deleteVirtualCurrency(currencyName: String, applicationName: String): Try[Unit] = {
      null
    }

    def getVirtualCurrency(currencyName: String, applicationName: String): Option[VirtualCurrency] = {
      databaseService.getElementFromArray[String](
        WazzaApplication.Key,
        applicationName,
        WazzaApplication.VirtualCurrenciesId,
        currencyName
      )
    }

    def getVirtualCurrencies(applicationName: String): List[VirtualCurrency] = {
      databaseService.getElementsOfArray(
        WazzaApplication.Key,
        applicationName,
        WazzaApplication.VirtualCurrenciesId,
        None
      ).map{el =>
        VirtualCurrency.buildFromJson(Some(el)).get
      }
    }

    def virtualCurrencyExists(currencyName: String, applicationName: String): Boolean = {
      false
    }
}
