package notifications.plugin.actors

import java.util.Date

 trait UserVoiceActor {

  def createTicket(timestamp: Date, error: Exception): Unit
}

