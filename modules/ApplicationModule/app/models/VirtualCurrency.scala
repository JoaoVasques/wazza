package models.application

import play.api.Play.current
import play.api.libs.json._
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

case class VirtualCurrency(
  name: String,
  price: Double,
  inAppPurchaseMetadata: InAppPurchaseMetadata,
  override val elementId: String = "name",
  override val attributeName: String = "virtualCurrencies"
) extends ApplicationList
