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
    case m: Exists => {}
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

  //Boolean
  def exists(msg: Exists) = {
    this.get(new Get(null, msg.collectionName, msg.key, msg.value)) map { result =>
      result match {
        case Some(_) => true
        case _ => false
      }
    }
  }

  //Option[JsValue]
  def get(msg: Get): Future[Option[JsValue]] = {

    val promise = Promise[Option[JsValue]]
    val query = MongoDBObject(msg.key -> msg.value)
    val proj = if(msg.projection != null) {
      MongoDBObject(msg.projection -> 1)
    } else {
      MongoDBObject()
    }

    Future {
      val res = collection(msg.collectionName).findOne(query,proj) match {
        case Some(obj) => Some(Json.parse(obj.toString))
        case _ => None
      }
      promise.success(res)

      if(msg.sendersStack != null) msg.sendersStack.head ! new PROptionResponse(msg.sendersStack, res)
    }
    promise.future
  }

  //List[JsValue]
  def getListElements(msg: GetListElements) = {

    val query = MongoDBObject(msg.key -> msg.value)
    val proj = if(msg.projection != null) {
      MongoDBObject(msg.projection -> 1)
    } else {
      MongoDBObject()
    }

    Future {
      val res = collection(msg.collectionName).find(query,proj).toList.map{(el: DBObject) => Json.parse(el.toString)}

      msg.sendersStack.head ! new PRListResponse(msg.sendersStack, res)
    }
  }

  // TODO
  def getElementsWithoutArrayContent(msg: GetElementsWithoutArrayContent) = {

    val query = msg.arrayKey $nin msg.array
    val projection = MongoDBObject(msg.arrayKey -> 1)
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
    }
  }

  // List[JsValue]
  def getCollectionElements(msg: GetCollectionElements) = {
    
    Future {
      val res = collection(msg.collectionName).find().toList map {(el: DBObject) => Json.parse(el.toString)}

      msg.sendersStack.head ! new PRListResponse(msg.sendersStack, res)
    }
  }

  def insert(msg: Insert) = {
    Future {
      if(msg.extra == null) {
        collection(msg.collectionName).insert(JSON.parse(msg.model.toString).asInstanceOf[DBObject])
      } else {
        val builder = MongoDBObject.newBuilder
        builder += "created_at" -> (msg.model \ "created_at").as[String]
        builder += "user_id" -> msg.extra("user_id")
        builder += "purchase_id" -> msg.extra("purchase_id")
        collection(msg.collectionName).insert(builder.result)
      }
    }
  }

  def delete(msg: Delete) = {
    Future {
      val element = JSON.parse(msg.el.toString).asInstanceOf[DBObject]
      collection(msg.collectionName).remove(JSON.parse(msg.el.toString).asInstanceOf[DBObject])
    }
  }

  def update(msg: Update) = {
    val query = MongoDBObject(msg.key -> msg.keyValue)
    val update = $set(msg.valueKey -> (JSON.parse(msg.newValue.toString).asInstanceOf[DBObject]))
    Future {
      collection(msg.collectionName).update(query, update)
    }
  }

  /**
    Time-ranged queries
    **/
  def getDocumentsWithinTimeRange(msg: GetDocumentsWithinTimeRange) = {
    val query = (msg.dateFields._1 $gte msg.start.getTime $lte msg.end.getTime) ++ (msg.dateFields._2 $gte msg.start.getTime $lte msg.end.getTime)
    val sortCriteria = MongoDBObject(msg.dateFields._1 -> 1)

    Future {
      val lst = collection(msg.collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      msg.sendersStack.head ! new PRJsArrayResponse(msg.sendersStack, new JsArray(lst))
    }
  }

  //JsArray
  def getDocumentsByTimeRange(msg: GetDocumentsByTimeRange) = {
    val query = (msg.dateField $lte msg.start.getTime $gt msg.end.getTime)
    val sortCriteria = MongoDBObject(msg.dateField -> 1)

    Future {
      val lst = collection(msg.collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      msg.sendersStack.head ! new PRJsArrayResponse(msg.sendersStack, new JsArray(lst))
    }
  }

  /**
    Array operations
    **/

  // Boolean
  def existsInArray[T <: Any](msg: ExistsInArray[T]) = {
    this.getElementFromArray[T](
      new GetElementFromArray[T](
        null,
        msg.collectionName,
        msg.docIdKey,
        msg.docIdValue,
        msg.arrayKey,
        msg.elementKey,
        msg.elementValue
      )
    ) map { res =>
      res match {
        case Some(_) => true
        case None => false
      }
    }
  }

  // Option[JsValue]
  def getElementFromArray[T <: Any](msg: GetElementFromArray[T]) = {

    def findElementAux(array: JsArray): Option[JsValue] = {
      array.value.find{ el=> {
        (el \ msg.elementKey).as[String].filter(_ != '"').equals(msg.elementValue)
      }}
    }

    val promise = Promise[Option[JsValue]]
    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val projection = MongoDBObject(msg.arrayKey -> 1)
    Future {
      val list = collection(msg.collectionName).find(query,projection).toList map {(el: DBObject) => Json.parse(el.toString)}
      val res = if(list.isEmpty)
        None
      else {
        findElementAux((list.head \ msg.arrayKey).as[JsArray])
      }

      if(msg.sendersStack != null) {
        msg.sendersStack.head ! new PROptionResponse(msg.sendersStack, res)
      }
      promise.success(res)
    }
    promise.future
  }

  // List[JsValue]
  def getElementsOfArray(msg: GetElementsOfArray) = {

    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val projection = MongoDBObject(msg.arrayKey -> 1)
    Future {
          val res = collection(msg.collectionName).find(query, projection).toList map {(el: DBObject) => Json.parse(el.toString)}
      msg.sendersStack.head ! new PRListResponse(msg.sendersStack, res)
      /**limit match {
        case Some(maxNumberElements) => {
          //TODO
          
        }
        case None => {
          val res = collection(msg.collectionName).find(query, projection).toList map {(el: DBObject) => Json.parse(el.toString)}
          
        }
      }**/
    }
  }

  def addElementToArray[T <: Any](msg: AddElementToArray[T]) = {
    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val m = msg.model match {
      case j: JsObject => JSON.parse(j.toString).asInstanceOf[DBObject]
      case _ => msg.model
    }
    val update = $push(msg.arrayKey -> m)
    Future {
      collection(msg.collectionName).update(query, update)
    }
  }

  def deleteElementFromArray[T <: Any](msg: DeleteElementFromArray[T]) = {
    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val update = $pull(
      msg.arrayKey -> MongoDBObject(
        msg.elementKey -> JSON.parse(msg.elementValue.toString).asInstanceOf[DBObject])
    )

    Future {
      collection(msg.collectionName).update(query, update)
    }
  }

  def updateElementOnArray[T <: Any](msg: UpdateElementOnArray[T]) = {

    val model = msg.m match {
      case j: JsObject => JSON.parse(j.toString).asInstanceOf[DBObject]
      case _ => msg.m
    }

    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue, s"${msg.arrayKey}.${msg.elementId}" -> msg.elementIdValue)
    val update = $set((msg.arrayKey+".$." + msg.elementId) -> model)

    Future {
      collection(msg.collectionName).update(query, update)
    }
  }
}

