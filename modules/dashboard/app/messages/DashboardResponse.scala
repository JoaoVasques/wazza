package dashboard.messages

import common.messages._
import akka.actor.{ActorRef}
import scala.collection.mutable.Stack
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import models.user._
import models.application._

trait DashboardResponse[T] extends WazzaMessage {
  val res: T
}

case class DROverviewResponse(
  var sendersStack: Stack[ActorRef],
  res: Tuple2[WazzaApplication, User],
  hash: String = null
) extends DashboardResponse[Tuple2[WazzaApplication, User]]

case class DRSettingsResponse(
  var sendersStack: Stack[ActorRef],
  res: Tuple2[WazzaApplication, User],
    hash: String = null
) extends DashboardResponse[Tuple2[WazzaApplication, User]]

