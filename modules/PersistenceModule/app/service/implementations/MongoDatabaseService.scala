package service.persistence.implementations

import com.mongodb.WriteResult
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON
import java.text.SimpleDateFormat
import play.api.Play
import play.api.libs.json._
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.persistence.definitions.{DatabaseService}
import scala.language.implicitConversions
import com.mongodb.casbah.Imports._

class MongoDatabaseService extends DatabaseService {

  private val mongoClient = getMongoClient().get
  private val collections = new HashMap[String, MongoCollection] with SynchronizedMap[String, MongoCollection]

  private def getCollection(collectionName: String): MongoCollection = {
    collections.synchronized {
      collections.get(collectionName) match {
        case Some(c) => c
        case None => {
          addCollection(collectionName)
          getCollection(collectionName)
        }
      }
    }
  }

  private def collectionExists(collectionName: String): Boolean = {
    collections.synchronized {
      collections.keySet.find(_ == collectionName) match {
        case Some(_) => true
        case None => false
      }
    }
  }

  private def addCollection(collectionName: String) = {
    collections.synchronized {
      if(!collections.keySet.exists(_ == collectionName)) {
        collections.put(collectionName, this.mongoClient.getDB(this.databaseName)(collectionName))
      }
    }
  }

  private var databaseName = getDatabaseName().get

  private def getDatabaseName(): Try[String] = {
    Play.current.configuration.getConfig("mongodb.dev") match {
      case Some(config) => {
        val uriStr = config.underlying.root.get("uri").render.filter(_ != '"')
        new Success(uriStr.split("/").last)
      }
      case _ => Failure(new Exception("MongoDb credentials do not exist"))
    }
  }

  private def getMongoClient(): Try[MongoClient] = {
    Play.current.configuration.getConfig("mongodb.dev") match {
      case Some(config) => {
        val uriStr = config.underlying.root.get("uri").render.filter(_ != '"')
        val uri = MongoClientURI(uriStr)
        new Success(MongoClient(uri))
      }
      case _ => Failure(new Exception("MongoDb credentials do not exist"))
    }
  }

  def dropCollection(collectionName: String): Unit = {
    val collection = this.getCollection(collectionName)
    collections.remove(collectionName)
    collection.drop()
  }

  private implicit def errorCheck(res: WriteResult): Try[Unit] = {
    if(res.getError == null) {
      new Success
    } else {
      Failure(new Exception(res.getError))
    }
  }

  private implicit def convertJsonToDBObject(json: JsValue): DBObject = {

    def convertDates(dateKey: String, dbObject: DBObject): DBObject = {
      if(dbObject.containsField(dateKey)) {
        val timeBackup = dbObject.get(dateKey).toString
        dbObject.removeField(dateKey)
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
        dbObject.put(dateKey, format.parse(timeBackup))
      }
      dbObject
    }

    val dateKeys = List("time", "startTime")
    val dbObject = JSON.parse(json.toString).asInstanceOf[DBObject]
    val res = dateKeys.filter(dbObject.containsField(_)).map {(key: String) =>
      convertDates(key, dbObject)
    }
    if(res.isEmpty) {
      dbObject
    } else {
      res.head
    }
  }

  def exists(collectionName: String, key: String, value: String): Boolean = {
    this.get(collectionName, key, value) match {
      case Some(_) => true
      case None => false
    }
  }

  def get(collectionName: String, key: String, value: String, projection: String = null): Option[JsValue] = {
    val query = MongoDBObject(key -> value)
    val proj = if(projection != null) {
      MongoDBObject(projection -> 1)
    } else {
      MongoDBObject()
    }

    val collection = this.getCollection(collectionName)
    collection.findOne(query, proj) match {
      case Some(obj) => {
        Some(Json.parse(obj.toString))
      }
      case _ => None
    }
  }

  def getCollectionElements(collectionName: String): List[JsValue] = {
    this.getCollection(collectionName).find.toList.map {el =>
      Json.parse(el.toString)
    }
  }

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Try[Unit] = {
    val collection = this.getCollection(collectionName)
    if(extra == null) {
      collection.insert(model)
    } else {
      // for the specific case of recommendation collection
      val builder = MongoDBObject.newBuilder
      builder += "created_at" -> (model \ "created_at").as[String]
      builder += "user_id" -> extra("user_id")
      builder += "purchase_id" -> extra("purchase_id")
      collection.insert(builder.result())
    }
  }

  def delete(collectionName: String, model: JsValue): Try[Unit] = {
    val collection = this.getCollection(collectionName)
    collection.remove(model)
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Try[Unit] = {
    val query = MongoDBObject(key -> keyValue)
    val update = $set(valueKey -> newValue)
    val collection = this.getCollection(collectionName)
    collection.update(query, update)
  }

  /**
    Array operations
  **/

  def existsInArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Boolean = {
    this.getElementFromArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementKey, elementValue) match {
      case Some(_) => true
      case None => false
    }
  }
 
  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Option[JsValue] = {

    def findElementAux(array: JsArray): Option[JsValue] = {  
       array.value.find{ el=> {
         (el \ elementKey).as[String].filter(_ != '"').equals(elementValue)
      }}
    }

    val query = MongoDBObject(docIdKey -> docIdValue)
    val projection = MongoDBObject(arrayKey -> 1)
    val collection = this.getCollection(collectionName)
    collection.findOne(query, projection) match {
      case Some(obj) => findElementAux((Json.parse(obj.toString) \ arrayKey).as[JsArray])   
      case _ => None
    }
  }


  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): List[JsValue] = {
    val query = MongoDBObject(docIdKey -> docIdValue)
    val projection = MongoDBObject(arrayKey -> 1)
    val collection = this.getCollection(collectionName)
    limit match {
      case Some(maxNumberElements) => {
        collection.find(query, projection).limit(maxNumberElements).map{el =>
          Json.parse(el.toString)
        }.toList
      }
      case None => {
        collection.find(query, projection).map{el =>
          Json.parse(el.toString)
        }.toList
      }
    }
  }

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    m: T
  ): Try[Unit] = {
    val query = MongoDBObject(docIdKey -> docIdValue)

    val model = m match {
      case j: JsObject => {
        convertJsonToDBObject(j)
      }
      case _ => m
    }

    val update = $push(arrayKey -> model)
    val collection = this.getCollection(collectionName)
    collection.update(query, update)
  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Try[Unit] = {
    val query = MongoDBObject(docIdKey -> docIdValue)
    val update = $pull(arrayKey -> MongoDBObject(elementKey -> elementValue))
    val collection = this.getCollection(collectionName)
    collection.update(query, update)
  }

  def updateElementOnArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Try[Unit] = {

    val model = m match {
      case j: JsObject => {
        convertJsonToDBObject(j)
      }
      case _ => m
    }

    val query = MongoDBObject(docIdKey -> docIdValue, s"$arrayKey.$elementId" -> elementIdValue)
    val update = $set((arrayKey+".$." + elementId) -> model)
    val collection = this.getCollection(collectionName)
    collection.update(query, update)
  }
}

