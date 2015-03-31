package models.payments

import scala.language.implicitConversions
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.util.Date
import models.common._
import scala.util.{Try, Failure, Success}


case class InAppPurchasePayment(
  id: String,
  sessionId: String,
  userId: String,
  itemId: String,
  price: Double,
  time: Date,
  deviceInfo: DeviceInfo,
  location: Option[LocationInfo],
  success: Boolean,
  paymentSystem: Int = InAppPurchasePayment.Type
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
          "paymentSystem" -> this.paymentSystem
        )
      }
      case None => {
        Json.obj(
          "id" -> this.id,
          "sessionId" -> this.sessionId,
          "userId" -> this.userId,
          "itemId" -> this.itemId,
          "price" -> this.price,
          "time" -> this.time.getTime,
          "device" -> Json.toJson(this.deviceInfo),
          "success" -> this.success,
          "paymentSystem" -> this.paymentSystem
        )
      }
    }
    null
  }
}

object InAppPurchasePayment {

  val Type = 1

  def fromJson(json: JsValue): Try[InAppPurchasePayment] = {
    try {
      val res = new InAppPurchasePayment(
        (json \ "id").as[String],
        (json \ "sessionId").as[String],
        (json \ "userId").as[String],
        (json \ "itemId").as[String],
        (json \ "price").as[Double],
        new Date((json \ "time").as[Long]),
        (json \ "device").validate[DeviceInfo].asOpt.get,
        PurchaseInfo.getLocation(json),
        (json \ "success").as[Boolean],
        InAppPurchasePayment.Type
      )
      new Success(res)
    } catch {
      case ex: Exception => new Failure(ex)
    }
  }
}

