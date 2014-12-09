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
import scala.reflect.ClassTag
import persistence.MongoFactory

class PersistenceWorker extends Actor with Worker[PersistenceMessage]  {
 
  def receive = {
    case m: Exists => exists(m, sender)
    case m: Get => get(m, sender)
    case m: GetListElements => getListElements(m, sender)
    case m: GetElementsWithoutArrayContent => getElementsWithoutArrayContent(m, sender)
    case m: GetCollectionElements => getCollectionElements(m, sender)
    case m: Insert => insert(m, sender)
    case m: Delete => delete(m , sender)
    case m: Update => update(m, sender)
    case m: GetDocumentsWithinTimeRange => getDocumentsWithinTimeRange(m, sender)
    case m: GetDocumentsByTimeRange => getDocumentsByTimeRange(m, sender)
    case m: ExistsInArray[_] => {} //TODO
    case m: GetElementFromArray[_] => {
      m.elementValue match {
        case _: String => getElementFromArray[String](m.asInstanceOf[GetElementFromArray[String]], sender)
        case _: JsValue => getElementFromArray[JsValue](m.asInstanceOf[GetElementFromArray[JsValue]], sender)
        case _: Int => getElementFromArray[Int](m.asInstanceOf[GetElementFromArray[Int]], sender)
      }
    }
    case m: GetElementsOfArray => getElementsOfArray(m, sender)
    case m: AddElementToArray[_] => {
      m.model match {
        case _: String => addElementToArray[String](m.asInstanceOf[AddElementToArray[String]], sender)
        case _: JsValue => addElementToArray[JsValue](m.asInstanceOf[AddElementToArray[JsValue]], sender)
        case _: Int => addElementToArray[Int](m.asInstanceOf[AddElementToArray[Int]], sender)
      }
    }
    case m: DeleteElementFromArray[_] => {} //TODO
    case m: UpdateElementOnArray[_] => {} //TODO
  }

  private def sendResponse[R <: PersistenceMessage](request: R,  msg: PersistenceResponse[_],  sender: ActorRef) = {
    if(request.direct) {
      sender ! msg
    } else {
      msg.sendersStack.pop ! msg
    }
  }

  private def collection(name: String) = {
    MongoFactory.getCollection(name)
  }

  //Boolean
  def exists(msg: Exists, sender: ActorRef) = {
    this.get(new Get(null, msg.collectionName, msg.key, msg.value), sender) map { result =>
      result match {
        case Some(_) => true
        case _ => false
      }
    }
  }

  //Option[JsValue]
  def get(msg: Get, sender: ActorRef): Future[Option[JsValue]] = {

    println("GET " + msg)
    println("sender " + sender)
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
      
      sendResponse[Get](msg, new PROptionResponse(msg.sendersStack, res, hash = msg.hash), sender)
      promise.success(res)
    }
    promise.future
  }

  //List[JsValue]
  def getListElements(msg: GetListElements, sender: ActorRef) = {

    val query = MongoDBObject(msg.key -> msg.value)
    val proj = if(msg.projection != null) {
      MongoDBObject(msg.projection -> 1)
    } else {
      MongoDBObject()
    }

    Future {
      val res = collection(msg.collectionName).find(query,proj).toList.map{(el: DBObject) => Json.parse(el.toString)}
      sendResponse[GetListElements](msg, new PRListResponse(msg.sendersStack, res, hash = msg.hash), sender)
    }
  }

  // TODO
  def getElementsWithoutArrayContent(msg: GetElementsWithoutArrayContent, sender: ActorRef) = {

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
  def getCollectionElements(msg: GetCollectionElements, sender: ActorRef) = {
    
    Future {
      val res = collection(msg.collectionName).find().toList map {(el: DBObject) => Json.parse(el.toString)}
      sendResponse[GetCollectionElements](msg, new PRListResponse(msg.sendersStack, res, hash = msg.hash), sender)
    }
  }

  def insert(msg: Insert, sender: ActorRef) = {
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

    if(msg.sendersStack.isEmpty)
      sendResponse[Insert](msg, new PRInsertResponse(msg.sendersStack, msg.model, hash = msg.hash), sender)
  }

  def delete(msg: Delete, sender: ActorRef) = {
    Future {
      val element = JSON.parse(msg.el.toString).asInstanceOf[DBObject]
      collection(msg.collectionName).remove(JSON.parse(msg.el.toString).asInstanceOf[DBObject])
    }
  }

  def update(msg: Update, sender: ActorRef) = {
    val query = MongoDBObject(msg.key -> msg.keyValue)
    val update = $set(msg.valueKey -> (JSON.parse(msg.newValue.toString).asInstanceOf[DBObject]))
    Future {
      collection(msg.collectionName).update(query, update)
    }
  }

  /**
    Time-ranged queries
    **/
  def getDocumentsWithinTimeRange(msg: GetDocumentsWithinTimeRange, sender: ActorRef) = {
    val query = (msg.dateFields._1 $gte msg.start.getTime $lte msg.end.getTime) ++ (msg.dateFields._2 $gte msg.start.getTime $lte msg.end.getTime)
    val sortCriteria = MongoDBObject(msg.dateFields._1 -> 1)

    Future {
      val lst = collection(msg.collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      sendResponse[GetDocumentsWithinTimeRange](msg, new PRJsArrayResponse(msg.sendersStack, new JsArray(lst), hash = msg.hash), sender)
    }
  }

  //JsArray
  def getDocumentsByTimeRange(msg: GetDocumentsByTimeRange, sender: ActorRef) = {
    val query = (msg.dateField $lte msg.start.getTime $gt msg.end.getTime)
    val sortCriteria = MongoDBObject(msg.dateField -> 1)

    Future {
      val lst = collection(msg.collectionName).find(query).sort(sortCriteria).toList map {(el: DBObject) => Json.parse(el.toString)}
      sendResponse[GetDocumentsByTimeRange](msg, new PRJsArrayResponse(msg.sendersStack, new JsArray(lst), hash = msg.hash), sender)
    }
  }

  /**
    Array operations
    **/

  // Boolean
  def existsInArray[T <: Any](msg: ExistsInArray[T], sender: ActorRef) = {
    this.getElementFromArray[T](
      new GetElementFromArray[T](
        null,
        msg.collectionName,
        msg.docIdKey,
        msg.docIdValue,
        msg.arrayKey,
        msg.elementKey,
        msg.elementValue
      ),
      sender
    ) map { res =>
      res match {
        case Some(_) => true
        case None => false
      }
    }
  }

  // Option[JsValue]
  def getElementFromArray[T <: Any](msg: GetElementFromArray[T], sender: ActorRef) = {

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

      sendResponse[GetElementFromArray[T]](msg, new PROptionResponse(msg.sendersStack, res, hash = msg.hash), sender)
      promise.success(res)
    }
    promise.future
  }

  // List[JsValue]
  def getElementsOfArray(msg: GetElementsOfArray, sender: ActorRef) = {

    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val projection = MongoDBObject(msg.arrayKey -> 1)
    Future {
      val res = collection(msg.collectionName).find(query, projection).toList map {(el: DBObject) => Json.parse(el.toString)}
      sendResponse[GetElementsOfArray](msg, new PRListResponse(msg.sendersStack, res, hash = msg.hash), sender)
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

  def addElementToArray[T <: Any](msg: AddElementToArray[T], sender: ActorRef) = {
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

  def deleteElementFromArray[T <: Any](msg: DeleteElementFromArray[T], sender: ActorRef) = {
    val query = MongoDBObject(msg.docIdKey -> msg.docIdValue)
    val update = $pull(
      msg.arrayKey -> MongoDBObject(
        msg.elementKey -> JSON.parse(msg.elementValue.toString).asInstanceOf[DBObject])
    )

    Future {
      collection(msg.collectionName).update(query, update)
    }
  }

  def updateElementOnArray[T <: Any](msg: UpdateElementOnArray[T], sender: ActorRef) = {

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

