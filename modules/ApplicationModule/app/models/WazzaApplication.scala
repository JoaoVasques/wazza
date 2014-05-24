package models.application

import play.api.Play.current
import play.api.libs.json._
import scala.language.implicitConversions
import play.api.libs.functional.syntax._

case class Credentials(
  appId: String,
  apiKey: String,
  sdkKey: String
)

object Credentials {
  implicit val reader = (
    (__ \ "appId").read[String] and
    (__ \ "apiKey").read[String] and
    (__ \ "sdkKey").read[String]
  )(Credentials.apply _)

  implicit val write = (
    (__ \ "appId").write[String] and
    (__ \ "apiKey").write[String] and
    (__ \ "sdkKey").write[String]
  )(unlift(Credentials.unapply))

  implicit def toJson(credential: Credentials): JsValue = {
    Json.obj(
      "appId" -> credential.appId,
      "apiKey" -> credential.apiKey,
      "sdkKey" -> credential.sdkKey
    )
  }

  implicit def fromJson(json: JsValue): Credentials = {
    new Credentials(
      (json \ "appId").as[String],
      (json \ "apiKey").as[String],
      (json \ "sdkKey").as[String]
    )
  }
}

case class WazzaApplication(
  name: String,
  appUrl: String,
  var imageName: String,
  packageName: String,
  appType: List[String],
  credentials: Credentials,
  items: List[Item] = List[Item](),
  virtualCurrencies: List[VirtualCurrency] = List[VirtualCurrency]()
)

object WazzaApplication {
  lazy val Key = "name"
  lazy val ItemsId = "items"
  lazy val VirtualCurrenciesId = "virtualCurrencies"
  lazy val CredentialsId = "credentials"
  lazy val applicationTypes = List("iOS", "Android")
}

package object WazzaApplicationImplicits {

  implicit def buildFromJson(json: JsValue): WazzaApplication = {
    new WazzaApplication(
      (json \ "name").as[String],
      (json \ "appUrl").as[String],
      (json \ "imageName").as[String],
      (json \ "packageName").as[String],
      (json \ "appType").as[List[String]],
      (json \ "credentials").validate[Credentials].get,
      (json \ "items").as[JsArray],
      (json \ "virtualCurrencies").as[JsArray]
    )
  }

  implicit def buildOptionFromOptionJson(json: Option[JsValue]): Option[WazzaApplication] = {
    json match {
      case Some(app) => Some(buildFromJson(app))
      case None => None
    }
  }

  implicit def convertToJson(application: WazzaApplication): JsValue = {
    Json.obj(
      "name" -> application.name,
      "appUrl" -> application.appUrl,
      "imageName" -> application.imageName,
      "packageName" -> application.packageName,
      "appType" -> application.appType,
      "credentials" -> Json.toJson(application.credentials),
      "items" -> JsArray(application.items.map{item =>
        Item.convertToJson(item)
      }.toSeq),
      "virtualCurrencies" -> JsArray(application.virtualCurrencies.map {vc =>
        VirtualCurrency.buildJson(vc)
      })
    )
  }

  implicit def buildOptionCredentialsFromJson(json: Option[JsValue]): Option[Credentials] = {
    json match {
      case Some(j) => {
          (j \ WazzaApplication.CredentialsId).validate[Credentials].fold(
          valid = {c => Some(c)},
          invalid = {_ => None}
        )
      }
      case None => None
    }
  }
}

