package models.user

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._

case class WazzaUser(
 id: ObjectId = new ObjectId,
 name: String,
 @Key("email") email: String,
 var password: String,
 company: String,
 permission: String = "Administrator"
)

object WazzaUser extends ModelCompanion[WazzaUser, ObjectId] {
	val dao = new SalatDAO[WazzaUser, ObjectId](collection = mongoCollection("users")) {}

	def getDAO(): SalatDAO[WazzaUser, ObjectId] = {
		dao
	}
}

sealed trait Permission
case object Administrator extends Permission
case object NormalUser extends Permission

object Permission {

  def valueOf(value: String): Permission = value match {
    case "Administrator" => Administrator
    case "NormalUser"    => NormalUser
    case _ => throw new IllegalArgumentException()
  }
}
