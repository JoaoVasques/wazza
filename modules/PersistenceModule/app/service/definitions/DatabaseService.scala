package service.persistence.definitions

import com.mongodb.casbah.MongoCollection
import play.api.libs.json.JsValue
import scala.util.Try

trait DatabaseService {

  protected var collection: MongoCollection = null

  lazy val ApplicationCollection = "applications"
  lazy val UserCollection = "users"
  lazy val PurchasesCollection = "purchases"

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

  def existsInArray(docIdKey: String, docIdValue: String, arrayKey: String, elementKey: String, elementValue: String): Boolean

  def getElementFromArray(docIdKey: String, docIdValue: String, arrayKey: String, elementKey: String, elementValue: Any): Option[JsValue]

  def addElementToArray[T <: Any](docIdKey: String, docIdValue: Any, arrayKey: String, model: T): Try[Unit]

  def deleteElementFromArray[T <: Any](docIdKey: String, docIdValue: Any, arrayKey: String, m: T): Try[Unit]

}

