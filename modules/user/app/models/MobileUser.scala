package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import java.util.Date

case class SessionResume(id: String, startTime: Date)
case class PurchaseResume(id: String, time: Date)
case class MobileUser(
  userId: String,
  sessions: List[SessionResume],
  purchases: List[PurchaseResume],
  devices: List[DeviceInfo]
)

object MobileUser {

  lazy val KeyId = "userId"
  lazy val SessionsKey = "sessions"
  lazy val PurchasesKey = "purchases"

  def getCollection(companyName: String, applicationName: String) = s"${companyName}_mUsers_${applicationName}"

  implicit def readJson(mobileUser: MobileUser): JsValue = {
    Json.obj(
      "userId" -> mobileUser.userId,
      "sessions" -> mobileUser.sessions.map(readJsonSessionResume(_)),
      "purchases" -> mobileUser.purchases.map(readJsonPurchaseResume(_)),
      "devices" -> List[DeviceInfo]() //TODO
    )
  }

  implicit def buildFromJson(json: JsValue): MobileUser = {
    new MobileUser(
      (json \ "userId").as[String],
      (json \ "sessions").as[JsArray].value.toList.map(buildSessionResumeFromJson(_)),
      (json \ "purchases").as[JsArray].value.toList.map(buildPurchaseResumeFromJson(_)),
      List() //TODO
    )
  }

  implicit def readJsonSessionResume(sessionResume: SessionResume): JsValue = {
    Json.obj(
      "id" -> sessionResume.id,
      "startTime" -> sessionResume.startTime.getTime
    )
  }

  implicit def buildSessionResumeFromJson(json: JsValue): SessionResume = {
    new SessionResume(
      (json \ "id").as[String],
      new Date((json \ "startTime").as[Long])
    )
  }

  implicit def readJsonPurchaseResume(purchaseResume: PurchaseResume): JsValue = {
    Json.obj("id" -> purchaseResume.id, "time" -> purchaseResume.time.getTime)
  }

  implicit def buildPurchaseResumeFromJson(json: JsValue): PurchaseResume = {
    new PurchaseResume(
      (json \ "id").as[String],
      new Date((json \ "time").as[Long])
    )
  }
}

