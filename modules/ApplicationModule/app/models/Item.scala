package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import ApplicationMongoContext._

case class Item(
  @Key("_id") name: String,
  description: String,
  store: Int,
  metadata: InAppPurchaseMetadata,
  currency: Currency,
  override val elementId: String = "name",
  override val attributeName: String = "items"
) extends ApplicationList

case class Currency(
  typeOf: Int, //virtual or real money
  value: Double
)

object Item extends ModelCompanion[Item, ObjectId] {

  val dao = new SalatDAO[Item, ObjectId](mongoCollection("applications")){}

  def getDAO = dao
}
