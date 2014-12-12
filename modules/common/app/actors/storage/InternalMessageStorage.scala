package common.actors

import akka.actor.{ActorRef}
import common.messages._
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.mutable.Map

class InternalMessageStorage[T <: WazzaMessage] {

  case class StorageElement(originalRequest: T, sender: ActorRef)

  private val BASE = 32
  private val BITS = 130
  private val random = new SecureRandom
  private def generateId = new BigInteger(BITS, random).toString(32)

  private val storage: Map[String, StorageElement] = Map()

  def store(sender: ActorRef, msg: T): String = {
    val id = generateId
    storage += (id -> new StorageElement(msg, sender))
    id
  }

  def get(id: String): Option[StorageElement] = {
    val s = storage.get(id)
    storage -= id
    s
  }
}
