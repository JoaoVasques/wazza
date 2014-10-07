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
    null
  }

  def get(
    collectionName: String,
    key: String,
    value: String,
    projection: String = null
  ): Future[Option[JsValue]] = {
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

  def insert(
    collectionName: String,
    model: JsValue,
    extra: Map[String, ObjectId] = null
  ): Future[Unit] = {
    null
  }

  def delete(collectionName: String, model: JsValue): Future[Unit] = {
    null
  }

  def update(
    collectionName: String,
    key: String,
    keyValue: String,
    valueKey: String,
    newValue: Any
  ): Future[Unit] = {
    null
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
    docIdValue: String,
    arrayKey: String,
    m: T
  ): Future[Unit] = {
    null
  }

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Future[Unit] = {
    null
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
    null
  }
}

