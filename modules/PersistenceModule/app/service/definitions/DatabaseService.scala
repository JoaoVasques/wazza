package service.persistence.definitions

import com.mongodb.casbah.MongoCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.util.Try

trait DatabaseService {

  protected var collection: MongoCollection = null

  lazy val ApplicationCollection = "applications"
  lazy val UserCollection = "users"
  lazy val PurchasesCollection = "purchases"

  def dropCollection(): Unit

  def init(collectionName: String): Try[Unit]

  def init(uri: String, collectionName: String)

  def hello(): Unit

  def exists(key: String, value: String): Boolean

  def get(key: String, value: String): Option[JsValue]

  def insert(model: JsValue): Try[Unit]

  def delete(el: JsValue): Try[Unit]

  def update(key: String, keyValue: String, valueKey: String, newValue: Any): Try[Unit]

  /**
    Array operations
  **/

  def existsInArray[T <: Any](
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Boolean

  def getElementFromArray[T <: Any](
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementKey: String,
    elementValue: T
  ): Option[JsValue]

  def getElementsOfArray(
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    limit: Option[Int]
  ): List[JsValue]

  def addElementToArray[T <: Any](
      docIdKey: String,
      docIdValue: Any,
      arrayKey: String,
      model: T
    ): Try[Unit]

  def deleteElementFromArray[T <: Any](
    docIdKey: String,
    docIdValue: Any,
    arrayKey: String,
    elementKey: String,
    elementValue:T
  ): Try[Unit]

  def updateElementOnArray[T <: Any](
    docIdKey: String,
    docIdValue: String,
    arrayKey: String,
    elementId: String,
    elementIdValue: String,
    m: T
  ): Try[Unit]
}


