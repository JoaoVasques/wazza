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

class ApplicationServiceImpl extends ApplicationService {
    
    private val dao = WazzaApplication.getDAO

    def insertApplication(application: WazzaApplication): Unit = {
        if(! exists(application.name)){
            dao.insert(application)
        } else {
            // throw exception "application with the same id already exists"
        }
    }

    def deleteApplication(name: String): Unit = {
        if(exists(name)){
            val application = find(name).get
            dao.remove(application)
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

    def addItem(item: Item, applicationName: String): Unit = {
        if(exists(applicationName) && ! itemExists(item.id, applicationName)){
            dao.update(
                MongoDBObject("name" -> applicationName),
                $push("items" -> grater[Item].asDBObject(item))
            )
        }
    }

    def getItem(itemName: String, applicationName: String): Option[Item] = {
        val listOfItems = dao.primitiveProjection[List[BasicDBObject]](MongoDBObject("items.name" -> itemName), "items")
        listOfItems match {
            case Some(_) => {
                val set = listOfItems.head.toSet.map((el: BasicDBObject) => Json.parse(el.toString))
                set.find((el: JsValue) => { 
                     (el \ "name").as[String] == itemName 
                })
            }
            case None => None
        }
    }

    def itemExists(itemId: String, applicationName: String): Boolean = {
        if(exists(applicationName)){
            !dao.find(MongoDBObject("items.id" -> itemId)).isEmpty
        } else {
            false // throw exception "application does not exist"
        }
    }

    def deleteItem(itemId: String, applicationName: String) = {}
}
