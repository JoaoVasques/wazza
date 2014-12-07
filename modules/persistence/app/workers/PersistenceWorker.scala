package persistence.worker

import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, Props}
import play.api.libs.concurrent.Akka._
import persistence.messages._
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
import com.mongodb.casbah.Imports.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import org.joda.time.DateTime
import scala.collection.immutable.StringOps
import persistence.MongoFactory

class PersistenceWorker extends Actor with Worker {

  def receive = {
    case m: Get => {}
    case m: GetListElements => {}
    case m: GetElementsWithoutArrayContent => {}
    case m: GetCollectionElements => {}
    case m: Insert => {}
    case m: Delete => {}
    case m: Update => {}
    case m: GetDocumentsWithinTimeRange => {}
    case m: GetDocumentsByTimeRange => {}
    case m: ExistsInArray[_] => {}
    case m: GetElementFromArray[_] => {}
    case m: GetElementsOfArray => {}
    case m: AddElementToArray[_] => {}
    case m: DeleteElementFromArray[_] => {}
    case m: UpdateElementOnArray[_] => {}
    case m => println("persistence worker received a message: " + m)
  }

  private def collection(name: String) = {
    MongoFactory.getCollection(name)
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

    val promise = Promise[Option[JsValue]]
    val query = MongoDBObject(key -> value)
    val proj = if(projection != null) {
      MongoDBObject(projection -> 1)
    } else {
      MongoDBObject()
    }

    Future {
      collection(collectionName).findOne(query,proj) match {
        case Some(obj) => promise.success(Some(Json.parse(obj.toString)))
        case _ => promise.success(None)
      }    
    }
    
    promise.future
  }

  def getListElements(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[List[JsValue]] = {

    val promise = Promise[List[JsValue]]
    val query = MongoDBObject(key -> value)
    val proj = if(projection != null) {
      MongoDBObject(projection -> 1)
    } else {
      MongoDBObject()
    }

    Future {
      val res = collection(collectionName).find(query,proj).toList.map{(el: DBObject) => Json.parse(el.toString)}
      promise.success(res)
    }
    
    promise.future
  }

  // TODO
  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): Future[List[JsValue]] = {

    val promise = Promise[List[JsValue]]
    val query = arrayKey $nin array
    val projection = MongoDBObject(arrayKey -> 1)
    Future {
      /**val output = collection(collectionName).find(query, projection).toList.map{(el: DBObject) => Json.parse(el.toString)}
      val r = output map {list =>
        var elements = List[JsValue]()
        list foreach {(el: JsObject) =>
          (el \ arrayKey).as[JsArray].value.foreach(i => {elements ::= i})
        }

        val result = elements.filter(el => {
          !array.contains((el \ elementKey).as[String])
        })

        if(limit > 0) result.take(limit) else result
      }**/
      promise.success(List())
    }

    promise.future
  }
  
  def getCollectionElements(collectionName: String): Future[List[JsValue]] = {
    val promise = Promise[List[JsValue]]
    Future {
      val res = collection(collectionName).find().toList map {(el: DBObject) => Json.parse(el.toString)}
      promise.success(res)
    }
    promise.future
  }

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Future[Unit] = {
    val promise = Promise[Unit]

    Future {
      if(extra == null) {
        promise.success(collection(collectionName).insert(JSON.parse(model.toString).asInstanceOf[DBObject]))
      } else {
        val builder = MongoDBObject.newBuilder
        builder += "created_at" -> (model \ "created_at").as[String]
        builder += "user_id" -> extra("user_id")
        builder += "purchase_id" -> extra("purchase_id")
        promise.success(collection(collectionName).insert(builder.result))
      }
    }  
    
    promise.future
  }

  def delete(collectionName: String, el: JsValue): Future[Unit] = {
    val promise = Promise[Unit]
    Future {
      val element = JSON.parse(el.toString).asInstanceOf[DBObject]
      promise.success(collection(collectionName).remove(JSON.parse(el.toString).asInstanceOf[DBObject]))
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
    val query = MongoDBObject(key -> keyValue)
    val update = $set(valueKey -> (JSON.parse(newValue.toString).asInstanceOf[DBObject]))
    Future {
      promise.success(collection(collectionName).update(query, update))
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
    val promise = Promise[JsArray]
    val query = (dateFields._1 $gte start.getTime $lte end.getTime) ++ (dateFields._2 $gte start.getTime $lte end.getTime)
    val sortCriteria = MongoDBObject(dateFields._1 -> 1)

    Future {
      val lst = collection(collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      promise.success(new JsArray(lst))
    }

    promise.future
  }

  def getDocumentsByTimeRange(
    collectionName: String,
    dateField: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    val promise = Promise[JsArray]
    val query = (dateField $lte start.getTime $gt end.getTime)
    val sortCriteria = MongoDBObject(dateField -> 1)

    Future {
      val lst = collection(collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      promise.success(new JsArray(lst))
    }

    promise.future
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

    val promise = Promise[Option[JsValue]]
    val query = MongoDBObject(docIdKey -> docIdValue)
    val projection = MongoDBObject(arrayKey -> 1)
    Future {
      val list = collection(collectionName).find(query,projection).toList map {(el: DBObject) => Json.parse(el.toString)}
      if(list.isEmpty)
        promise.success(None)
      else {
        promise.success(findElementAux((list.head \ arrayKey).as[JsArray]))
      }
    }
    promise.future
  }

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): Future[List[JsValue]] = {

    val promise = Promise[List[JsValue]]
    val query = MongoDBObject(docIdKey -> docIdValue)
    val projection = MongoDBObject(arrayKey -> 1)
    Future {
      limit match {
        case Some(maxNumberElements) => {
          //TODO
          val res = collection(collectionName).find(query, projection).toList map {(el: DBObject) => Json.parse(el.toString)}
          promise.success(res)
        }
        case None => {
          val res = collection(collectionName).find(query, projection).toList map {(el: DBObject) => Json.parse(el.toString)}
          promise.success(res)
        }
      }
    }
    
    promise.future
  }

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    model: T
  ): Future[Unit] = {
    val promise = Promise[Unit]
    val query = MongoDBObject(docIdKey -> docIdValue)
    val m = model match {
      case j: JsObject => JSON.parse(j.toString).asInstanceOf[DBObject]
      case _ => model
    }
    val update = $push(arrayKey -> m)
    Future {
      promise.success(collection(collectionName).update(query, update))
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
    val query = MongoDBObject(docIdKey -> docIdValue)
    val update = $pull(
      arrayKey -> MongoDBObject(
        elementKey -> JSON.parse(elementValue.toString).asInstanceOf[DBObject])
    )

    Future {
      promise.success(collection(collectionName).update(query, update))
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
      case j: JsObject => JSON.parse(j.toString).asInstanceOf[DBObject]
      case _ => m
    }

    val query = MongoDBObject(docIdKey -> docIdValue, s"$arrayKey.$elementId" -> elementIdValue)
    val update = $set((arrayKey+".$." + elementId) -> model)

    Future {
      promise.success(collection(collectionName).update(query, update))
    }

    promise.future
  }
}

