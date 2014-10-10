package service.persistence.implementations

import com.mongodb.WriteResult
import com.mongodb.util.JSON
import java.util.Date
import play.api.Play
import play.api.libs.json._
import service.persistence.definitions.{DatabaseService}
import scala.language.implicitConversions
import com.mongodb.casbah.Imports._
import scala.concurrent._

class MongoDatabaseService extends DatabaseService {

  def exists(collectionName: String, key: String, value: String): Future[Boolean] = {
    actor.exists(collectionName, key, value)
  }

  def get(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[Option[JsValue]] = {
    actor.get(collectionName, key, value, projection)
  }

  def getListElements(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[List[JsValue]] = {
    actor.getListElements(collectionName, key, value, projection)
  }

  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): Future[List[JsValue]] = {
    actor.getElementsWithoutArrayContent(collectionName, arrayKey, elementKey, array, limit)
  }

  def getCollectionElements(collectionName: String): Future[List[JsValue]] = {
    actor.getCollectionElements(collectionName)
  }

  def insert(
    collectionName: String,
    model: JsValue,
    extra: Map[String, ObjectId] = null
  ): Future[Unit] = {
    actor.insert(collectionName, model, extra)
  }

  def delete(collectionName: String, model: JsValue): Future[Unit] = {
    actor.delete(collectionName, model)
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Future[Unit] = {
    actor.update(collectionName, key, keyValue, valueKey, newValue)
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
    actor.getDocumentsWithinTimeRange(collectionName, dateFields, start, end)
  }

  def getDocumentsByTimeRange(
    collectionName: String,
    dateField: String,
    start: Date,
    end: Date
  ): Future[JsArray] = {
    actor.getDocumentsByTimeRange(collectionName, dateField, start, end)
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
    actor.existsInArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementKey, elementValue)
  }
 
  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Option[JsValue]] = {
    actor.getElementFromArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementKey, elementValue)
  }

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): Future[List[JsValue]] = {
    actor.getElementsOfArray(collectionName, docIdKey, docIdValue, arrayKey, limit)
  }

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    m: T
  ): Future[Unit] = {
    actor.addElementToArray[T](collectionName, docIdKey, docIdValue, arrayKey, m)
  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Unit] = {
    actor.deleteElementFromArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementKey, elementValue)
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
    actor.updateElementOnArray[T](collectionName, docIdKey, docIdValue, arrayKey, elementId, elementIdValue, m)
  }
}

