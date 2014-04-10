package actors.recommendation

import models.application.Item
import models.user.MobileUser

sealed trait PredictionMessages

case class AddUserMessage(user: MobileUser) extends PredictionMessages

case class AddItemMessage(item: Item) extends PredictionMessages
