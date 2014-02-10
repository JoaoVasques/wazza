package service.application.implementations

import service.application.definitions._
import models.application._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import ApplicationMongoContext._
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import InAppPurchaseContext._
import scala.util.{Try, Success, Failure}
import scala.reflect.runtime.universe._
import com.mongodb.casbah.commons.{MongoDBList}
import com.google.inject._
import service.aws.definitions.{PhotosService}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.implicitConversions

class ApplicationServiceImpl @Inject()(
    photoService: PhotosService
) extends ApplicationService with ApplicationErrors{
    
    private val dao = WazzaApplication.getDAO

    def createFailure[A](error: String): Failure[A] = {
        new Failure(new Exception(error))
    }

    def insertApplication(application: WazzaApplication): Try[WazzaApplication] = {
        if(! exists(application.name)){
            dao.insert(application)
            Success(application)
        } else {
            createFailure[WazzaApplication](ApplicationWithNameExistsError(application.name))
        }
    }

    def deleteApplication(name: String): Try[WazzaApplication] = {
        if(exists(name)){
            val application = find(name).get
            dao.remove(application)
            new Success(application)
        } else {
            createFailure[WazzaApplication]("Application " + name + " does not exists")
        }
    }

    def exists(name: String): Boolean = {
        find(name) match {
            case Some(_) => true
            case _ => false
        }
    }

    def find(key: String): Option[WazzaApplication] = {
        dao.findOne(MongoDBObject("name" -> key))
    }

    def getApplicationyTypes: List[String] = {
        WazzaApplication.applicationTypes
    }

    def getApplicationCountries(appName: String): List[String] = {
        List("PT")
    }

    private def addDocumentToArray[T <: ApplicationList](doc: T, value: String, applicationName: String): Try[T] = {
        if(! exists(applicationName)){
            createFailure[T](DoesNotExistError)
        } else {
            if(existsDocumentInArray(doc.attributeName, doc.elementId, value, applicationName)){
                createFailure[T](AlreadyExistsError)
            } else {
                doc match {
                    case item: Item => {
                        dao.update(
                            MongoDBObject("name" -> applicationName),
                            $push(item.attributeName -> grater[Item].asDBObject(item))
                        )
                    } 
                    case virtualCurrency: VirtualCurrency => {
                        dao.update(
                            MongoDBObject("name" -> applicationName),
                            $push(virtualCurrency.attributeName -> grater[VirtualCurrency].asDBObject(virtualCurrency))
                        )
                    }
                }
                new Success(doc)
            }
        }
    }

    private def deleteDocumentFromArray[T <: ApplicationList](
        applicationAttribute: String,
        key: String,
        value: String,
        applicationName: String
    ): Try[Unit] = {
        if(existsDocumentInArray(applicationAttribute, key, value, applicationName)){
            dao.update(
                MongoDBObject("name" -> applicationName),
                $pull(applicationAttribute -> MongoDBObject(key -> value))
            )
            Success()
        } else {
            createFailure[Unit]("Does not exist")
        }
    }

    private def existsDocumentInArray(
        applicationAttribute: String,
        key: String,
        value: String,
        applicationName: String
    ): Boolean = {
        if(exists(applicationName)){
          !dao.find(MongoDBObject(s"$applicationAttribute.$key" -> value)).isEmpty
        } else {
            false
        }
    }

    private def getDocumentInArray(
        applicationAttribute: String,
        key: String,
        value: String,
        applicationName: String
    ): Option[JsValue] = {
        val list = dao.primitiveProjection[List[BasicDBObject]](MongoDBObject(s"$applicationAttribute.$key" -> value), applicationAttribute)
        list match {
            case Some(_) => {
                val set = list.head.toSet.map((el: BasicDBObject) => Json.parse(el.toString))
                set.find((el: JsValue) => { 
                    (el \ key).as[String] == value 
                })
            }
            case None => None
        }
    }

    def addItem(item: Item, applicationName: String): Try[Item] = {
        addDocumentToArray[Item](item, item.name, applicationName)
    }

    def getItem(itemId: String, applicationName: String): Option[Item] = {
        getDocumentInArray("items", "_id", itemId, applicationName)
    }

    def getItems(applicationName: String, offset: Int = 0): List[Item] = {
        // WARNING: this is inefficient because it loads all items from DB. For now, just works... to be fixed later
        this.find(applicationName) match {
            case Some(application) => application.items.drop(offset).take(ItemBatch)
            case None => Nil
        }
    }

    def itemExists(keyValue: String, applicationName: String, key: String = "name"): Boolean = {
        if(key == "name"){
            existsDocumentInArray("items", "_id", keyValue, applicationName)
        } else {
            existsDocumentInArray("items", "metadata.itemId", keyValue, applicationName)
        }
    }

    def deleteItem(itemId: String, applicationName: String, imageName: String): Future[Unit] = {
        val promise = Promise[Unit]
        val result = deleteDocumentFromArray[Item]("items", "_id", itemId, applicationName)
        result match {
            case Success(_) => {
                photoService.delete(imageName) map {res =>
                    promise.success()
                } recover {
                    case err: Exception => promise.failure(err)
                }
            }
            case Failure(failure) => promise.failure(failure)
        }
        promise.future
    }

    def addVirtualCurrency(currency: VirtualCurrency, applicationName: String): Try[VirtualCurrency] = {
        addDocumentToArray[VirtualCurrency](currency, currency.name, applicationName)
    }
  
    def deleteVirtualCurrency(currencyName: String, applicationName: String): Try[Unit] = {
        deleteDocumentFromArray[VirtualCurrency]("virtualCurrencies", "name", currencyName, applicationName)
    }

    def getVirtualCurrency(currencyName: String, applicationName: String): Option[VirtualCurrency] = {
        getDocumentInArray("virtualCurrencies", "name", currencyName, applicationName)
    }

    def getVirtualCurrencies(applicationName: String): List[VirtualCurrency] = {
        val list = dao.primitiveProjection[List[BasicDBObject]](MongoDBObject("name" -> applicationName), "virtualCurrencies")
        list match {
            case Some(_) => {
                list.head.toSet.map((el: BasicDBObject) => Json.parse(el.toString))
            }
            case None => Nil
        }
    }

    def virtualCurrencyExists(currencyName: String, applicationName: String): Boolean = {
        existsDocumentInArray("virtualCurrencies", "name", currencyName, applicationName)
    }

    implicit def convertListJsonToItem(jsonList: List[JsValue]): List[Item] = {
        jsonList.map {el =>
            new Item(
                (el \ "_id").as[String],
                (el \ "description").as[String],
                (el \ "store").as[Int],
                (el \ "metadata"),
                (el \ "currency"),
                new ImageInfo(
                    (el \ "name").as[String],
                    (el \ "url").as[String]
                )
            )
        }
    }
}
