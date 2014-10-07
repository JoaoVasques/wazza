package persistence.plugin.actors

import java.util.Date
import org.bson.types.ObjectId
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.util.Try
import scala.concurrent._

protected[plugin] trait DatabaseActor {

  def exists(collectionName: String, key: String, value: String): Future[Boolean]

  def get(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[Option[JsValue]]

  def getListElements(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[List[JsValue]]

  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): Future[List[JsValue]]
  
  def getCollectionElements(collectionName: String): Future[List[JsValue]]

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Future[Unit]

  def delete(collectionName: String, el: JsValue): Future[Unit]

  def update(collectionName: String, key: String, keyValue: String, valueKey: String, newValue: Any): Unit

  /**
    Time-ranged queries
    **/
  def getDocumentsWithinTimeRange(
    collectionName: String,
    dateFields: Tuple2[String, String],
    start: Date,
    end: Date
  ): Future[JsArray]

  def getDocumentsByTimeRange(
    collectionName: String,
    dateField: String,
    start: Date,
    end: Date
  ): Future[JsArray]

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
  ): Future[Boolean]

  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Option[JsValue]]

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): Future[List[JsValue]]

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    model: T
  ): Unit

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Unit

  def updateElementOnArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Unit
}

