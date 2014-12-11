package common.actors

import akka.actor.{ActorRef}
import common.messages._
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

/**
  S - List of all data that was sent for a given request
  R - List of all received results of that request
**/
case class Element[S](originalRequest: WazzaMessage, sendedData: List[S], sender: ActorRef) {

  private var results = new ListBuffer[Any]()

  def hasReceivedAllResults = sendedData.size == results.size

  def addResult[N](newResult: N) = {
    results += newResult
    results = results.distinct
  }

  def getResults = results.toList
}

class DependencyStorage {



  private val BASE = 32
  private val BITS = 130
  private val random = new SecureRandom
  private def generateId = new BigInteger(BITS, random).toString(32)

  private val storage: Map[String, Element[_]] = Map()

  def store[S](sender: ActorRef, sendedData: List[S], originalReq: WazzaMessage): String = {
    val id = generateId
    storage += (id -> new Element[S](originalReq, sendedData, sender))
    id
  }

  def gotAllResults(hash: String): Boolean = {
    storage.get(hash) match {
      case Some(entry) => entry.hasReceivedAllResults
      case _ => false
    }
  }

  def saveResult[R](hash: String, result: R) = {
    storage.get(hash) match {
       case Some(entry) => entry.addResult(result)
      case _ => {
        //TODO log error and launch exception
      }
    }
  }

  def get[S](hash: String): Option[Element[S]] = {
    val res = storage.get(hash) match {
      case Some(element) => Some(element.asInstanceOf[Element[S]])
      case _ => None
    }
    storage -= hash
    res
  }

  def getOriginalRequest(hash: String): Option[WazzaMessage] = {
    storage.get(hash) match {
      case Some(element) => Some(element.originalRequest)
      case _ => None
    }
  }
}

trait DependencyStorageDecorator {
  val dependencyStorage = new DependencyStorage
}

