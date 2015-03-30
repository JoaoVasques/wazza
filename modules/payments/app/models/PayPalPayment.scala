package models.payments

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.util.Date
import models.common._
import scala.util.{Try, Failure, Success}

case class PayPalPayment(
  id: String,
  sessionId: String,
  userId: String,
  itemId: String,
  price: Double,
  time: Date,
  deviceInfo: DeviceInfo,
  location: Option[LocationInfo],
  success: Boolean,
  paymentSystem: Int = PayPalPayment.Type,
  quantity: Int,
  currency: String,
  payPalResponseId: String,
  responseType: String
) extends PurchaseInfo {

  def toJson(): JsValue = {
    this.location match {
      case Some(_) => {
        Json.obj(
          "id" -> this.id,
          "sessionId" -> this.sessionId,
          "userId" -> this.userId,
          "itemId" -> this.itemId,
          "price" -> this.price,
          "time" -> this.time.getTime,
          "device" -> Json.toJson(this.deviceInfo),
          "location" -> Json.toJson(this.location),
          "success" -> this.success,
          "paymentSystem" -> this.paymentSystem,
          "quantity" -> this.quantity,
          "currency" -> this.currency,
          "responseID" -> this.payPalResponseId,
          "responseType" -> this.responseType
        )
      }
      case _ => {
        Json.obj(
          "id" -> this.id,
          "sessionId" -> this.sessionId,
          "userId" -> this.userId,
          "itemId" -> this.itemId,
          "price" -> this.price,
          "time" -> this.time.getTime,
          "device" -> Json.toJson(this.deviceInfo),
          "success" -> this.success,
          "paymentSystem" -> this.paymentSystem,
          "quantity" -> this.quantity,
          "currency" -> this.currency,
          "responseID" -> this.payPalResponseId,
          "responseType" -> this.responseType
        )
      }
    }
  }
}

object PayPalPayment {

  val Type = 2

  def fromJson(json: JsValue): Try[PayPalPayment] = {
    try {
      val res = new PayPalPayment(
        (json \ "id").as[String],
        (json \ "sessionId").as[String],
        (json \ "userId").as[String],
        (json \ "itemId").as[String],
        (json \ "price").as[Double],
        new Date((json \ "time").as[Long]),
        (json \ "deviceInfo").validate[DeviceInfo].asOpt.get,
        PurchaseInfo.getLocation(json),
        (json \ "success").as[Boolean],
        PayPalPayment.Type,
        (json \ "quantity").as[Int],
        (json \ "currencyCode").as[String],
        (json \ "responseID").as[String],
        (json \ "responseType").as[String]
      )
      new Success(res)
    } catch {
      case ex: Exception => new Failure(ex)
    }
  }
}

