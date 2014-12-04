package notifications.plugins

import play.api.Play.current

trait UserVoiceConnector {

  def actor = UserVoicePlugin.actor
}
