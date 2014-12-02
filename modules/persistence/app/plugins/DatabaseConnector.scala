package persistence.plugins

import play.api.Play.current

trait DatabaseConnector {

  def actor = DatabasePlugin.actor
}

