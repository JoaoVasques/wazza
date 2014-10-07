package persistence.plugin.actors

import java.text.SimpleDateFormat
import java.util.Date
import org.bson.types.ObjectId
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.util.Try
import play.api.libs.functional.syntax._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import scala.language.implicitConversions
import com.mongodb.casbah.Imports._
import scala.util.{Failure, Success}
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger

// Reactive Mongo imports
import reactivemongo.api._

protected[plugin] class MongoActor extends DatabaseActor {

  private val db = Play.application.plugin[ReactiveMongoPlugin]
    .getOrElse(throw new RuntimeException("ReactiveMongoPlugin not loaded")).helper.db

  private def collection(name: String): JSONCollection = {
    db.collection[JSONCollection](name)
  }

  def exists(collectionName: String, key: String, value: String): Future[Boolean] = {
    this.get(collectionName, key, value) map { result =>
      result match {
        case Some(_) => true
        case _ => false
      }
    }
  }

  def get(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[Option[JsValue]] = {

    val query = Json.obj(key -> value)
    val proj = if(projection != null) {
      Json.obj(projection -> 1)
    } else {
      Json.obj()
    }

    val cursor = collection(collectionName).find(query,proj).cursor[JsObject]
    cursor.collect[List]() map { results =>
      if(results.isEmpty) None else Some(results.head)
    }
  }

  def getListElements(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[List[JsValue]] = {

    val query = Json.obj(key -> value)
    val proj = if(projection != null) {
      Json.obj(projection -> 1)
    } else {
      Json.obj()
    }

    collection(collectionName).find(query,proj).cursor[JsObject].collect[List]()
  }

  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): Future[List[JsValue]] = {
    /**

      val query = (arrayKey $nin array)
      val projection = MongoDBObject(arrayKey -> 1)
      val collection = this.getCollection(collectionName)
      var elements = List[JsValue]()
      for(el <- collection.find(query, projection)) {
      (Json.parse(el.toString) \ arrayKey).as[JsArray].value.foreach(item => {
      elements ::= Json.parse(item.toString)
      })
      }
      
      val result = elements.filter(el => {
      !array.contains((el \ elementKey).as[String])
      })

      if(limit > 0) {
      result.take(limit)
      } else {
      result
      }

      * */
    null
  }
  
  def getCollectionElements(collectionName: String): Future[List[JsValue]] = {
    collection(collectionName).find(Json.obj()).cursor[JsObject].collect[List]()
  }

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Future[Unit] = {
    //val promise = Promise[JsArray]

    /**
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
    **/
    if(extra == null) {
      collection(collectionName).insert(model)
    } else {

    }

    null
  }

  def delete(collectionName: String, el: JsValue): Future[Unit] = {
    collection(collectionName).remove(el) map {lastError =>
      Logger.info(s"DELETE Last error: $lastError")
    }
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Unit = {

    val query = Json.obj(key -> keyValue)
    val update = Json.parse(($set(valueKey -> newValue)).toString)
    collection(collectionName).update(query, update)
  }

  /**
    Time-ranged queries
    **/
  def getDocumentsWithinTimeRange(
    collectionName: String,
    dateFields: Tuple2[String, String],
    start: Date,
    end: Date
  ): Future[JsArray] = {

    val query = Json.parse(((dateFields._1 $lte start $gt end) ++ (dateFields._2 $lte start $gt end)).toString)
    val sortCriteria = Json.obj(dateFields._1 -> 1)
    collection(collectionName).find(query).sort(sortCriteria).cursor[JsObject].collect[List]() map {list =>
      new JsArray(list)
    }
  }

  def getDocumentsByTimeRange(
    collectionName: String,
    dateField: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {

    val query = Json.parse(((dateField $lte start $gt end)).toString)
    val sortCriteria = Json.obj(dateField -> 1)
    collection(collectionName).find(query).sort(sortCriteria).cursor[JsObject].collect[List]() map { list =>
      new JsArray(list)
    }
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
  ): Future[Boolean] = {
    this.getElementFromArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementKey, elementValue) map { res =>
      res match {
        case Some(_) => true
        case None => false
      }
    }
  }

  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Option[JsValue]] = {

    def findElementAux(array: JsArray): Option[JsValue] = {
      array.value.find{ el=> {
        (el \ elementKey).as[String].filter(_ != '"').equals(elementValue)
      }}
    }

    val query = Json.obj(docIdKey -> docIdValue)
    val projection = Json.obj(arrayKey -> 1)
    collection(collectionName).find(query,projection).cursor[JsObject].collect[List]() map {list =>
      if(list.isEmpty)
        None
      else {
        findElementAux((list.head \ arrayKey).as[JsArray])
      }
    }
  }

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): Future[List[JsValue]] = {

    val query = Json.obj(docIdKey -> docIdValue)
    val projection = Json.obj(arrayKey -> 1)
    limit match {
      case Some(maxNumberElements) => {
        null
        //TODO collection(collectionName).find(query, projection).limit(maxNumberElements).cursor[JsObject].collect[List]()
      }
      case None => {
        collection(collectionName).find(query, projection).cursor[JsObject].collect[List]()
      }
    }
  }

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    model: T
  ): Unit = {
    /**
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

      * */
    val query = Json.obj(docIdKey -> docIdValue)
    val update = Json.parse($push(arrayKey -> model).toString)
    //TODO
  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Unit = {
    val query = Json.obj(docIdKey -> docIdValue)
    val update = Json.parse($pull(arrayKey -> MongoDBObject(elementKey -> elementValue)).toString)
    collection(collectionName).update(query, update)
  }

  def updateElementOnArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Unit = {
    val query = Json.obj(docIdKey -> docIdValue, s"$arrayKey.$elementId" -> elementIdValue)
    //TODO
  }

  /**
    PRIVATE METHODS
  **/

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

    if(res.isEmpty) dbObject else res.head
  }
}

