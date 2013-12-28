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

case class WazzaApplication(
	name: String,
  appUrl: String,
  // image: String,
  storeId: String,
  packageName: String,
  appType: Option[String],
  credentials: Credentials,
	items: List[Item] = List[Item]()
)

case class Credentials(
  appId: String,
  apiKey: String,
  sdkKey: String
)

object WazzaApplication extends ModelCompanion[WazzaApplication, ObjectId] {

	val dao = new SalatDAO[WazzaApplication, ObjectId](mongoCollection("applications")){}
	def getDAO = dao
}
