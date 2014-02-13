package service.persistence.implementations

import com.mongodb.WriteResult
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON
import play.api.Play
import play.api.libs.json._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.persistence.definitions.{DatabaseService}
import scala.language.implicitConversions
import com.mongodb.casbah.Imports._

class MongoDatabaseService extends DatabaseService {

  private var databaseName = ""

  private def getMongoClient(): Try[MongoClient] = {
    Play.current.configuration.getConfig("mongodb.dev") match {
      case Some(config) => {
        println(config)
        val uriStr = config.underlying.root.get("uri").render.filter(_ != '"')
        val uri = MongoClientURI(uriStr)
        this.databaseName = uriStr.split("/").last
        new Success(MongoClient(uri))
      }
      case _ => Failure(new Exception("MongoDb credentials do not exist"))
    }
  }

  def init(collectionName: String): Try[Unit] = {
    getMongoClient() match {
      case Success(client) => {
        this.collection =  client.getDB(this.databaseName)(collectionName)
        new Success()
      }
      case Failure(e) => Failure(e)
    }
  }

  def init(uriStr: String, collectionName: String) = {
    val client = MongoClient( MongoClientURI(uriStr))
    this.databaseName = uriStr.split("/").last
    this.collection =  client.getDB(this.databaseName)(collectionName)
  }

  def hello(): Unit = {
    println("hello world")
  }

  private implicit def errorCheck(res: WriteResult): Try[Unit] = {
    if(res.getError == null) {
      new Success
    } else {
      Failure(new Exception(res.getError))
    }
  }

  private implicit def convertJsonToDBObject(json: JsValue): DBObject = {
    JSON.parse(json.toString).asInstanceOf[DBObject]
  }

  def exists(key: String, value: String): Boolean = {
    this.get(key, value) match {
      case Some(_) => true
      case None => false
    }
  }

  def get(key: String, value: String): Option[JsValue] = {
    val query = MongoDBObject(key -> value)
    this.collection.findOne(query) match {
      case Some(obj) => {       
        Some(Json.parse(obj.toString))
      }
      case _ => None
    }
  }

  def insert(model: JsValue): Try[Unit] = {
    this.collection.insert(model)    
  }

  def delete(model: JsValue): Try[Unit] = {
    this.collection.remove(model)
  }

  def update(
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Try[Unit] = {
    val query = MongoDBObject(key -> keyValue)
    val update = $set(valueKey -> newValue)
    this.collection.update(query, update)
  }

  /**
    Array operations
  **/

  def existsInArray[T <: Any](
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementValue: T
  ): Boolean = {
    this.getElementFromArray[T](docIdKey, docIdValue, arrayKey, elementValue) match {
      case Some(_) => true
      case None => false
    }
  }
 
  def getElementFromArray[T <: Any](
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementValue: T
  ): Option[JsValue] = {

    val element = elementValue match {
      case j: JsObject => {
       arrayKey $in List(convertJsonToDBObject(j))
      }
      case s: String => {
        arrayKey $in List(s)
      }
    }

    val query = element ++ MongoDBObject(docIdKey -> docIdValue)
    val projection = MongoDBObject(arrayKey -> 1)
    this.collection.findOne(query, projection) match {
      case Some(obj) => {
        Some(Json.parse(obj.toString))
      }
      case _ => None
    }
  }

  def addElementToArray[T <: Any](
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
    this.collection.update(query, update)
  }

  def deleteElementFromArray[T <: Any](
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

    val update = $pull(arrayKey -> model)
    this.collection.update(query, update)
  }

  def updateElementOnArray[T <: Any](
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
    this.collection.update(query, update)
  }
}

