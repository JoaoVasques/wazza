package models.payments

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.util.Date
import models.common._
import scala.util.{Try, Failure, Success}
import play.api.Logger

/**
  Purchase Id format: Hash(appName + itemID + time + device)
**/
abstract class PurchaseInfo {
  val id: String
  val sessionId: String
  val userId: String
  val itemId: String
  val price: Double
  val time: Date
  val deviceInfo: DeviceInfo
  val location: Option[LocationInfo]
  val paymentSystem: Int
  val success: Boolean

  def toJson(): JsValue

}

object PurchaseInfo {

  lazy val Id = "id"
  lazy val UserId = "userId"
  def getCollection(companyName: String, applicationName: String) = s"${companyName}_purchases_${applicationName}"

  def getLocation(json: JsValue) = {
    if(json.as[JsObject].keys.contains("location")) {
      (json \ "location").validate[LocationInfo].asOpt
    } else {
      None
    }
  }

  implicit def buildFromJson(json: JsValue): PurchaseInfo = {
    println("BUILD FROM JSON")
    val possibleResult = (json \ "paymentSystem").as[Int] match {
      case PayPalPayment.Type => PayPalPayment.fromJson(json)
      case InAppPurchasePayment.Type => InAppPurchasePayment.fromJson(json)
    }
    possibleResult match {
      case Success(result) => result
      case Failure(e) => {
        Logger.error(s"Error building payment info from json: ${json}\n${e}")
        throw e
      }
    }
  }
}

