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

// Reactive Mongo imports
import reactivemongo.api._

protected[plugin] class MongoActor extends DatabaseActor {

  val db = Play.application.plugin[ReactiveMongoPlugin]
    .getOrElse(throw new RuntimeException("MyPlugin not loaded")).helper.db

  private def collection(name: String): JSONCollection = {
    db.collection[JSONCollection](name)
  }

  def exists(collectionName: String, key: String, value: String): Future[Boolean] = {
    null
  }

  def get(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[Option[JsValue]] = {

    /**

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
    **/



    null
  }

  def getListElements(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[List[JsValue]] = {
    null
  }

  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): Future[List[JsValue]] = {
    null
  }
  
  def getCollectionElements(collectionName: String): Future[List[JsValue]] = {
    null
  }

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Future[Unit] = {
    null
  }

  def delete(collectionName: String, el: JsValue): Future[Unit] = {
    null
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Unit = {

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
    null
  }

  def getDocumentsByTimeRange(
    collectionName: String,
    dateField: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    null
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
    null
  }

  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Option[JsValue]] = {
    null
  }

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): Future[List[JsValue]] = {
    null
  }

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    model: T
  ): Unit = {

  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Unit = {

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

