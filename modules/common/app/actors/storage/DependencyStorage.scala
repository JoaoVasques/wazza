package common.actors

import akka.actor.{ActorRef}
import common.messages._
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

class DependencyStorage[T <: WazzaMessage] {

  /**
    S - List of all data that was sent for a given request
    R - List of all received results of that request
  **/
  case class Element[S, R](originalRequest: T, sendedData: List[S], sender: ActorRef) {

    private val results: ListBuffer[R] = ListBuffer[R]()

    def hasReceivedAllResults = sendedData.size == results.size

    def addResult[N](newResult: N) = {
      newResult match {
        case r: R => {
          if(!results.exists(_ == r)) {
            results += r
          }
        }
        case _ => {
          //TODO Log
        }
      }
    }

    def getResults: List[R] = results.toList

  }

  private val BASE = 32
  private val BITS = 130
  private val random = new SecureRandom
  private def generateId = new BigInteger(BITS, random).toString(32)

  private val storage: Map[String, Element[_,_]] = Map()

  def store[S, R](sender: ActorRef, sendedData: List[S], originalReq: T): String = {
    val id = generateId
    storage += (id -> new Element[S,R](originalReq, sendedData, sender))
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
       case Some(entry) => entry.addResult[R](result)
      case _ => {
        //TODO log error and launch exception
      }
    }
  }

  def get[S, R](hash: String): Option[Element[S,R]] = {
    val res = storage.get(hash) match {
      case Some(element) => Some(element.asInstanceOf[Element[S,R]])
      case _ => None
    }
    storage -= hash
    res
  }
}

trait DependencyStorageDecorator[T <: WazzaMessage] {
  val dependencyStorage = new DependencyStorage[T]
}

