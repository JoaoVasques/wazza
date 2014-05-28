package service.persistence.definitions

import java.util.Date
import org.bson.types.ObjectId
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.util.Try

trait DatabaseService {

  lazy val ApplicationCollection = "applications"
  lazy val UserCollection = "users"
  lazy val PurchasesCollection = "purchases"

  def dropCollection(collectionName: String): Unit

  def exists(collectionName: String, key: String, value: String): Boolean

  def get(collectionName: String, key: String, value: String, projection: String = null): Option[JsValue]

  def getListElements(collectionName: String, key: String, value: String, projection: String = null): List[JsValue]

  def getElementsWithoutArrayContent(
    collectionName: String,
    arrayKey: String,
    elementKey: String,
    array: List[String],
    limit: Int
  ): List[JsValue]
                                                                                                 
  def getCollectionElements(collectionName: String): List[JsValue]

  def insert(collectionName: String, model: JsValue, extra: Map[String, ObjectId] = null): Try[Unit]

  def delete(collectionName: String, el: JsValue): Try[Unit]

  def update(collectionName: String, key: String, keyValue: String, valueKey: String, newValue: Any): Try[Unit]

  /**
    Time-ranged queries
  **/
  def getDocumentsWithinTimeRange(collectionName: String, dateFields: Tuple2[String, String], start: Date, end: Date): JsArray

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
  ): Boolean

  def getElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Option[JsValue]

  def getElementsOfArray(
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): List[JsValue]

  def addElementToArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    model: T
  ): Try[Unit]

  def deleteElementFromArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Try[Unit]

  def updateElementOnArray[T <: Any](
    collectionName: String,
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Try[Unit]
}


