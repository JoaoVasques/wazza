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
}

case class WazzaApplication(
  name: String,
  appUrl: String,
  var imageName: String,
  packageName: String,
  appType: Option[String],
  credentials: Credentials,
  items: List[Item] = List[Item](),
  virtualCurrencies: List[VirtualCurrency] = List[VirtualCurrency]()
)

object WazzaApplication {
  lazy val Key = "name"
  lazy val ItemsId = "items"
  lazy val VirtualCurrenciesId = "virtualCurrencies"
  lazy val applicationTypes = List("iOS", "Android")
}

package object WazzaApplicationImplicits {

  private abstract class ListBuilder[T] {
    def build(jsonArray: JsArray): List[T]
  }

  private implicit object ItemListBuilder extends ListBuilder[Item] {
    def build(jsonArray: JsArray): List[Item] = {
      jsonArray.value.map{(item: JsValue) =>
        item.validate[Item].fold(
          valid = {i => i},
          invalid = {_ => null}
        )
      }.toList
    }
  }

  private implicit object VirtualCurrencyListBuilder extends ListBuilder[VirtualCurrency] {
    def build(jsonArray: JsArray): List[VirtualCurrency] = {
      jsonArray.value.map {(vc: JsValue) =>
        vc.validate[VirtualCurrency].fold(
          valid = {i => i},
          invalid = {_ => null}
        )
      }.toList
    }
  }

  private def buildList[T](jsonArray: JsArray)(implicit builder: ListBuilder[T]): List[T] = {
    builder.build(jsonArray)
  }

  implicit def buildFromJson(json: JsValue): WazzaApplication = {
    new WazzaApplication(
      (json \ "name").as[String],
      (json \ "appUrl").as[String],
      (json \ "imageName").as[String],
      (json \ "packageName").as[String],
      (json \ "appType").asOpt[String],
      (json \ "credentials").validate[Credentials].get,
      buildList[Item]((json \ "items").as[JsArray]),
      buildList[VirtualCurrency]((json \ "items").as[JsArray])
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
        Json.toJson(item)
      }),
      "virtualCurrencies" -> JsArray(application.virtualCurrencies.map {vc =>
        Json.toJson(vc)
      })
    )
  }
}

