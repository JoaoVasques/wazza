package models.user

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import models.common._

case class SessionResume(id: String, startTime: Date, platform: String)
case class PurchaseResume(id: String, time: Date, platform: String)
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
      "devices" -> mobileUser.devices.map(Json.toJson(_))
    )
  }

  implicit def buildFromJson(json: JsValue): MobileUser = {
    new MobileUser(
      (json \ "userId").as[String],
      (json \ "sessions").as[JsArray].value.toList.map(buildSessionResumeFromJson(_)),
      (json \ "purchases").as[JsArray].value.toList.map(buildPurchaseResumeFromJson(_)),
      (json \ "devices").as[JsArray].value.toList.map(_.validate[DeviceInfo].asOpt.get)
    )
  }

  implicit def readJsonSessionResume(sessionResume: SessionResume): JsValue = {
    Json.obj(
      "id" -> sessionResume.id,
      "startTime" -> sessionResume.startTime.getTime,
      "platform" -> sessionResume.platform
    )
  }

  implicit def buildSessionResumeFromJson(json: JsValue): SessionResume = {
    new SessionResume(
      (json \ "id").as[String],
      new Date((json \ "startTime").as[Long]),
      (json \ "platform").as[String]
    )
  }

  implicit def readJsonPurchaseResume(purchaseResume: PurchaseResume): JsValue = {
    Json.obj(
      "id" -> purchaseResume.id,
      "time" -> purchaseResume.time.getTime,
      "platform" -> purchaseResume.platform
    )
  }

  implicit def buildPurchaseResumeFromJson(json: JsValue): PurchaseResume = {
    new PurchaseResume(
      (json \ "id").as[String],
      new Date((json \ "time").as[Long]),
      (json \ "platform").as[String]
    )
  }
}

