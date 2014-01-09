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

    def createFailure[A](error: String): Failure[A] = {
        new Failure(new Exception(error))
    }

    def insertApplication(application: WazzaApplication): Try[WazzaApplication] = {
        if(! exists(application.name)){
            dao.insert(application)
            new Success(application)
        } else {
            createFailure[WazzaApplication]("Application with the name " + application.name +  " already exists")
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

    def addItem(item: Item, applicationName: String): Try[Item] = {
        if(! exists(applicationName) ){
            createFailure[Item]("Application does not exist")
        } else {
            if(itemExists(item.name, applicationName)){
                createFailure[Item]("Item already exists")
            } else {
                dao.update(
                    MongoDBObject("name" -> applicationName),
                    $push("items" -> grater[Item].asDBObject(item))
                )
                new Success(item)
            } 
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

    def itemExists(keyValue: String, applicationName: String, key: String = "name"): Boolean = {
        if(exists(applicationName)){
            if(key == "name"){
              !dao.find(MongoDBObject("items._id" -> keyValue)).isEmpty
            } else {
                !dao.find(MongoDBObject("items.metadata.itemId" -> keyValue)).isEmpty
            }
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
            createFailure[Item]("Item with id " + itemId + " does not exist in application " + applicationName)
        }
    }
}
