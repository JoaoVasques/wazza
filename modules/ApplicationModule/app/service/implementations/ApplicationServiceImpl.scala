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
import ItemContext._
import scala.util.{Try, Success, Failure}

class ApplicationServiceImpl extends ApplicationService {
    
    private val dao = WazzaApplication.getDAO

    def insertApplication(application: WazzaApplication): Try[WazzaApplication] = {
        if(! exists(application.name)){
            dao.insert(application)
            new Success(application)
        } else {
            new Failure(
                new Exception("Application with the name " + application.name +  " already exists")
            )
        }
    }

    def deleteApplication(name: String): Try[WazzaApplication] = {
        if(exists(name)){
            val application = find(name).get
            dao.remove(application)
            new Success(application)
        } else {
            new Failure(
                new Exception("Application " + name + " does not exists")
            )
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

    def getApplicationyTypes: Map[String, String] = {
        WazzaApplication.applicationTypes
    }

    def addItem(item: Item, applicationName: String): Try[Item] = {
        if(exists(applicationName) && ! itemExists(item.name, applicationName)){
            dao.update(
                MongoDBObject("name" -> applicationName),
                $push("items" -> grater[Item].asDBObject(item))
            )
            new Success(item)
        } else {
            new Failure(
                new Exception("Duplicated item")
            )
        }
    }

    def getItem(itemId: String, applicationName: String): Option[Item] = {
        val listOfItems = dao.primitiveProjection[List[BasicDBObject]](MongoDBObject("items._id" -> itemId), "items")
        listOfItems match {
            case Some(_) => {
                val set = listOfItems.head.toSet.map((el: BasicDBObject) => Json.parse(el.toString))
                set.find((el: JsValue) => { 
                     (el \ "_id").as[String] == itemId 
                })
            }
            case None => None
        }
    }

    def itemExists(itemId: String, applicationName: String): Boolean = {
        if(exists(applicationName)){
            !dao.find(MongoDBObject("items._id" -> itemId)).isEmpty
        } else {
            false
        }
    }

    def deleteItem(itemId: String, applicationName: String): Try[Item] = {
        if(itemExists(itemId, applicationName)){
            val item = getItem(itemId, applicationName).get
            dao.update(
                MongoDBObject("name" -> applicationName),
                $pull("items" -> MongoDBObject("_id" -> itemId))
            )
            new Success(item)
        } else {
            new Failure(
                new Exception("Item with id " + itemId + " does not exist in application " + applicationName)
            )
        }
    }
}
