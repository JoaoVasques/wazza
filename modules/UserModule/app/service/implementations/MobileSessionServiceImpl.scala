package service.user.implementations

import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import scala.util.Failure
import scala.util.Success
import service.user.definitions.MobileSessionService
import models.user.MobileSession
import play.api.libs.json.JsValue
import scala.util.Try
import com.google.inject._
import service.persistence.definitions.DatabaseService
import models.user.MobileSessionInfo

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

  def update(id: String, date: String, purchaseId: String): Try[Unit] = {
    getSessionInfo(id) match {
      case Some(info) => {
        
        null
      }
      case None => new Failure(new Exception("Session does not exist"))
    }
  }
}
