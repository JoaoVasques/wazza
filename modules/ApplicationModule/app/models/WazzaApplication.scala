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
	items: List[Item] = List[Item]()
)

object WazzaApplication extends ModelCompanion[WazzaApplication, ObjectId] {

	val dao = new SalatDAO[WazzaApplication, ObjectId](mongoCollection("applications")){}
	def getDAO = dao
}
