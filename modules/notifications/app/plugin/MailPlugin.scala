package notifications.plugins

import play.api.{Plugin, Application}
import akka.actor.TypedActor
import akka.actor.TypedProps
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api._
import notifications.plugin.actors._
import play.api._

protected[plugins] case class MailCredentials(apiKey: String, endpoint: String)

protected[plugins] object MailCredentials {
  def apply(ops: (Option[String], Option[String])): Option[MailCredentials] = {
    ops match {
      case _ if(ops._1.isDefined && ops._2.isDefined) => {
        Some(new MailCredentials(ops._1.get, ops._2.get))
      }
      case _ => None
    }
  }
}

class MailPlugin(implicit app: Application) extends Plugin {

  private var _actor: Option[MailActor] = None
  def actor: MailActor = _actor.getOrElse(throw new RuntimeException("MailPlugin error: no plugin available?"))
                               
  override def onStart() = {   
    MailPlugin.parseConfig(app) match {
      case Some(credentials) => {
        _actor = Some(TypedActor(Akka.system).typedActorOf(TypedProps(classOf[MailActor],
          new MandrillActor(credentials.apiKey, credentials.endpoint)
        )))
        Logger.info("Starting Wazza Mail plugin")
      }
      case _ => {
        throw new PlayException("Wazza Mail","Credentials not found")
      }
    }
  }

  override def onStop() = {
    Logger.info("Shuting down Wazza Mail plugin")
    TypedActor(Akka.system).stop(_actor.get)
  }

  override def enabled = true
}

object MailPlugin {
  def actor(implicit app: Application) = current.actor

  def current(implicit app: Application): MailPlugin = app.plugin[MailPlugin] match {
    case Some(plugin) => plugin
    case _ => throw new PlayException("Wazza Mail","Error occurred while initializing MailPlugin")
  }

  private def parseConfig(app: Application): Option[MailCredentials] = {
    def getConfigElement(config: Configuration, key: String): Option[String] = {
      config.getString(key) match {
        case Some(element) => Some(element filterNot ("'" contains _))
        case _ => None
      }
    }

    app.configuration.getConfig("mandrill") match {
      case Some(conf) => {
        MailCredentials(
          getConfigElement(conf, "apiKey"),
          getConfigElement(conf, "endpoint")
        )
      }
      case _ => None
    }
  }
}

