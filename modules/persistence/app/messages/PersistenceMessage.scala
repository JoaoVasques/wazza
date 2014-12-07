package persistence.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import org.bson.types.ObjectId
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

trait PersistenceMessage extends WazzaMessage
case class Msg(sendersStack: Stack[ActorRef], hey: String) extends PersistenceMessage

case class Exists(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  key: String,
  value: String
) extends PersistenceMessage

case class Get(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  key: String,
  value: String,
  projection: String = null
) extends PersistenceMessage

case class GetListElements(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  key: String,
  value: String,
  projection: String = null
) extends PersistenceMessage

case class GetElementsWithoutArrayContent(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  arrayKey: String,
  elementKey: String,
  array: List[String],
  limit: Int
) extends PersistenceMessage

case class GetCollectionElements(sendersStack: Stack[ActorRef], collectionName: String) extends PersistenceMessage

case class Insert(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  model: JsValue,
  extra: Map[String, ObjectId] = null
) extends PersistenceMessage

case class Delete(sendersStack: Stack[ActorRef], collectionName: String, el: JsValue) extends PersistenceMessage

case class Update(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  key: String,
  keyValue: String,
  valueKey: String,
  newValue: Any
) extends PersistenceMessage

/**
  Time-ranged queries
  **/
case class GetDocumentsWithinTimeRange(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  dateFields: Tuple2[String, String],
  start: Date,
  end: Date
) extends PersistenceMessage

case class GetDocumentsByTimeRange(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  dateField: String,
  start: Date,
  end: Date
) extends PersistenceMessage

/**
  Array operations
  **/

case class ExistsInArray[T <: Any](
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  elementKey: String,
  elementValue: T
) extends PersistenceMessage

case class GetElementFromArray[T <: Any](
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  elementKey: String,
  elementValue: T
) extends PersistenceMessage

case class GetElementsOfArray(
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  limit: Option[Int]
) extends PersistenceMessage

case class AddElementToArray[T <: Any](
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  model: T
) extends PersistenceMessage

case class DeleteElementFromArray[T <: Any](
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  elementKey: String,
  elementValue:T
) extends PersistenceMessage

case class UpdateElementOnArray[T](
  sendersStack: Stack[ActorRef],
  collectionName: String,
  docIdKey: String,
  docIdValue: String,
  arrayKey: String,
  elementId: String,
  elementIdValue: String,
  m: T
) extends PersistenceMessage

