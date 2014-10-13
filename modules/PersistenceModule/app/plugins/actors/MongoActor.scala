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
import play.api.libs.json._

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
    } recover {
      case ex: Exception => {
        Logger.error(s"MongoActor: GET Error - " + ex.getMessage)
        throw ex
      }
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

    val query = Json.parse((arrayKey $nin array).toString)
    val projection = Json.obj(arrayKey -> 1)
    collection(collectionName).find(query, projection).cursor[JsObject].collect[List]() map {list =>
      var elements = List[JsValue]()
      list foreach {(el: JsObject) =>
        (el \ arrayKey).as[JsArray].value.foreach(i => {elements ::= i})
      }

      val result = elements.filter(el => {
        !array.contains((el \ elementKey).as[String])
      })

      if(limit > 0) result.take(limit) else result
    }
  }
  
  def getCollectionElements(collectionName: String): Future[List[JsValue]] = {
    collection(collectionName).find(Json.obj()).cursor[JsObject].collect[List]()
  }

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Future[Unit] = {
    val promise = Promise[Unit]
    if(extra == null) {
      collection(collectionName).insert(model) map { lastError =>
        lastError.err match {
          case Some(error) => {
            Logger.error(error)
            promise.failure(new Exception(error))
          }
          case _ => promise.success()
        }
      } recover {
        case ex: Exception => promise.failure(ex)
      }
    } else {
      val builder = MongoDBObject.newBuilder
      builder += "created_at" -> (model \ "created_at").as[String]
      builder += "user_id" -> extra("user_id")
      builder += "purchase_id" -> extra("purchase_id")
      collection(collectionName).insert(Json.parse(builder.result().toString)) map {lastError =>
        Logger.info(s"Mongo Actor: INSERT successfuly done")
        promise.success()
      } recover {
        case ex: Exception => promise.failure(ex)
      }
    }
    promise.future
  }

  def delete(collectionName: String, el: JsValue): Future[Unit] = {
    val promise = Promise[Unit]
    collection(collectionName).remove(el) map {lastError =>
      Logger.info(s"Mongo Actor: DELETE successfuly done")
      promise.success()
    } recover {
      case ex: Exception => {
        Logger.error("")
        promise.failure(ex)
      }
    }
    promise.future
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Future[Unit] = {
    val promise = Promise[Unit]
    val query = Json.obj(key -> keyValue)
    val update = Json.parse(($set(valueKey -> newValue)).toString)
    collection(collectionName).update(query, update) map { lastError =>
      Logger.info("Mongo Actor: UPDATE successfuly done")
      promise.success()
    } recover {
      case ex: Exception => {
        Logger.error("")
        promise.failure(ex)
      }
    }
    promise.future
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
        //TODO
        collection(collectionName).find(query, projection).cursor[JsObject].collect[List]()
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
  ): Future[Unit] = {
    val promise = Promise[Unit]
    val query = Json.obj(docIdKey -> docIdValue)
    val m = model match {
      case j: JsObject => convertStringToDateInJson(j)
      case _ => model
    }
    val update = Json.parse($push(arrayKey -> m).toString)
    collection(collectionName).update(query, update) map {lastError =>
      Logger.info("Mongo Actor: elemented added to array successfuly")
      promise.success()
    } recover {
      case ex: Exception => {
        Logger.error("")
        promise.failure(ex)
      }
    }
    promise.future
  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Future[Unit] = {
    val promise = Promise[Unit]
    val query = Json.obj(docIdKey -> docIdValue)
    val update = Json.parse($pull(arrayKey -> MongoDBObject(elementKey -> elementValue)).toString)
    collection(collectionName).update(query, update) map { lastError =>
      promise.success()
    } recover {
      case ex: Exception => {
        Logger.error("")
        promise.failure(ex)
      }
    }
    promise.future
  }

  def updateElementOnArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Future[Unit] = {

    val promise = Promise[Unit]
    val model = m match {
      case j: JsObject => convertStringToDateInJson(j)
      case _ => m
    }

    val query = Json.obj(docIdKey -> docIdValue, s"$arrayKey.$elementId" -> elementIdValue)
    val update = Json.parse($set((arrayKey+".$." + elementId) -> model).toString)
    collection(collectionName).update(query, update) map { lastError =>
      promise.success()
    } recover {
      case ex: Exception => {
        Logger.error("")
        promise.failure(ex)
      }
    }
    promise.future
  }

  /**
    PRIVATE METHODS
  **/

  private def convertStringToDateInJson(json: JsObject): JsObject = {
    def convertAux(field: String): JsObject = {
      if(json.keys.contains(field)) {
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
        val date = format.parse((json \ field).as[String])
        val transformer = (__ \ field).json.update(
          __.read[JsObject].map{o => Json.obj(field -> date)}
        )
        json.transform(transformer) fold(
          valid = {v => v},
          invalid = {errors => null}
        )
      } else json
    }

    val dateKeys = List("time", "startTime")
    var j = json
    dateKeys.filter(json.keys.contains(_)) foreach {(key: String) =>
      j = convertAux(key)
    }
    j
  }
}

