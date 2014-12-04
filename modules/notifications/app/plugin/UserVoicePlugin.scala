package notifications.plugins

import play.api.{Plugin, Application}
import akka.actor.TypedActor
import akka.actor.TypedProps
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api._
import notifications.plugin.actors._
import play.api._

protected[plugins] case class UserVoiceCredentials(subdomain: String, apiKey: String, secretKey: String)

protected[plugins] object UserVoiceCredentials {
  def apply(ops: (Option[String], Option[String], Option[String])): Option[UserVoiceCredentials] = {
    ops match {
      case _ if(ops._1.isDefined && ops._2.isDefined && ops._3.isDefined) => {
        Some(new UserVoiceCredentials(ops._1.get, ops._2.get, ops._3.get))
      }
      case _ => None
    }
  }
}

class UserVoicePlugin(implicit app: Application) extends Plugin {

  private var _actor: Option[UserVoiceActor] = None
  def actor: UserVoiceActor = _actor.getOrElse(throw new RuntimeException("UserVoicePlugin error: no plugin available?"))
                               
  override def onStart() = {   
    UserVoicePlugin.parseConfig(app) match {
      case Some(credentials) => {
        _actor = Some(TypedActor(Akka.system).typedActorOf(TypedProps(classOf[UserVoiceActor],
          new UserVoiceActorImpl(credentials.subdomain, credentials.apiKey, credentials.secretKey)
        )))
        Logger.info("Starting Wazza UserVoice plugin")
      }
      case _ => {
        throw new PlayException("Wazza UserVoice","Credentials not found")
      }
    }
  }

  override def onStop() = {
    Logger.info("Shuting down Wazza UserVoice plugin")
    TypedActor(Akka.system).stop(_actor.get)
  }

  override def enabled = true
}

object UserVoicePlugin {
  def actor(implicit app: Application) = current.actor

  def current(implicit app: Application): UserVoicePlugin = app.plugin[UserVoicePlugin] match {
    case Some(plugin) => plugin
    case _ => throw new PlayException("Wazza UserVoice","Error occurred while initializing UserVoicePlugin")
  }

  private def parseConfig(app: Application): Option[UserVoiceCredentials] = {
    def getConfigElement(config: Configuration, key: String): Option[String] = {
      config.getString(key) match {
        case Some(element) => Some(element filterNot ("'" contains _))
        case _ => None
      }
    }

    app.configuration.getConfig("uservoice") match {
      case Some(conf) => {
        UserVoiceCredentials(
          getConfigElement(conf, "subdomain"),
          getConfigElement(conf, "apiKey"),
          getConfigElement(conf, "secretKey")
        )
      }
      case _ => None
    }
  }
}

