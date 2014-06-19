package service.user.implementations

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import play.api.libs.json._
import scala.util.Failure
import scala.util.Success
import service.user.definitions.MobileSessionService
import models.user.MobileSession
import play.api.libs.json.JsValue
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService
import models.user.MobileSessionInfo
import com.github.nscala_time.time.Imports._

class MobileSessionServiceImpl @Inject()(
  databaseService: DatabaseService
) extends MobileSessionService {

  def create(json: JsValue): Try[MobileSession] = {
    (json).validate[MobileSession] match {
      case session: JsSuccess[MobileSession] => {
        Success(session.get)
      }
      case e: JsError => new Failure(new Exception(JsError.toFlatJson(e).toString))
    }
  }

  def insert(companyName: String, applicationName: String, session: MobileSession): Try[Unit] = {
    if(!exists(session.id)) {
      val collection = MobileSession.getCollection(companyName, applicationName)
      databaseService.insert(collection, Json.toJson(session)) match {
        case Success(_) => {
          addSessionToHashCollection(companyName, applicationName, session)
        }
        case Failure(f) => new Failure(f)
      }
    } else {
      new Failure(new Exception("mobile session already exists"))
    }
  }

  def get(hash: String): Option[MobileSession] = {
    getSessionInfo(hash) match {
      case Some(info) => {
        val collection = MobileSession.getCollection(info.companyName, info.applicationName)
        databaseService.get(
          collection,
          MobileSession.Id,
          hash
        ) match {
          case Some(json) => {
            val sessionMap = json.as[Map[String, JsValue]]
            val updated = sessionMap + ("startTime" -> sessionMap.get("startTime").map {d =>
              (d \ "$date").as[JsString]
            }.get)

            MobileSession.buildJsonFromMap(updated).validate[MobileSession].fold(
              valid = { s => Some(s) },
              invalid = {_ => None}
            )
          }
          case None => None
        }
      }
      case None => None
    }
  }

  private def addSessionToHashCollection(
    companyName: String,
    applicationName: String,
    session: MobileSession
  ): Try[Unit] = {
    val collection = MobileSessionInfo.collection
    val info = new MobileSessionInfo(
      session.id,
      session.userId,
      applicationName,
      companyName
    )
    databaseService.insert(collection, Json.toJson(info))
  }

  private def getSessionInfo(id: String): Option[MobileSessionInfo] = {
    if(exists(id)) {
      databaseService.get(
        MobileSessionInfo.collection,
        MobileSessionInfo.Id,
        id
      ) match {
        case Some(json) => {
          json.validate[MobileSessionInfo].fold(
            valid = { s => Some(s) },
            invalid = {_ => None}
          )
        }
        case None => None
      }
    } else {
      None
    }
  }

  def exists(id: String): Boolean = {
    val collection = MobileSessionInfo.collection
    databaseService.exists(collection, MobileSessionInfo.Id,id)
  }

  def addPurchase(companyName: String, applicationName: String, session: MobileSession, purchaseId: String) = {
    val collection = MobileSession.getCollection(companyName, applicationName)
    databaseService.addElementToArray[String](
      collection,
      MobileSession.Id,
      session.id,
      MobileSession.Purchases,
      purchaseId
    )
  }

  def calculateSessionLength(session: MobileSession, dateStr: String) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
    val start = format.parse(session.startTime)
    val end = format.parse(dateStr)
    val duration =  TimeUnit.MILLISECONDS.toSeconds(end.getTime - start.getTime)

    val info = getSessionInfo(session.id).get

    val collection = MobileSession.getCollection(info.companyName, info.applicationName)

    databaseService.update(
      collection,
      MobileSession.Id,
      session.id,
      "sessionLength",
      duration
    )

  }
}
