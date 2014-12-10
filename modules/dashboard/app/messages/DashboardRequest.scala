package dashboard.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

trait DashboardMessageRequest extends WazzaMessage {
  def direct: Boolean
}

case class DROverviewBootstrap(
  var sendersStack: Stack[ActorRef],
  userId: String,
  direct: Boolean = false,
  hash: String = null
) extends DashboardMessageRequest

case class DRSettingsBootstrap(
  var sendersStack: Stack[ActorRef],
  userId: String,
  direct: Boolean = false,
  hash: String = null
) extends DashboardMessageRequest

