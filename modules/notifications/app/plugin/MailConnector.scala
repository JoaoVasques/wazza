package notifications.plugins

import play.api.Play.current

trait MailConnector {

  def MailProxy = MailPlugin.actor
}
