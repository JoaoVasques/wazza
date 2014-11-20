package persistence.plugins

import play.api.{Plugin, Application}
import persistence.plugin.actors._
import akka.actor.TypedActor
import akka.actor.TypedProps
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api._

class KeyValueStorePlugin(implicit app: Application) extends Plugin {

  private var _kvActor: Option[KeyValueStoreActor] = None
  def dbActor = _kvActor.getOrElse(throw new RuntimeException("KeyValueStorePlugin error: no plugin available?"))

  override def onStart() = {
    _kvActor = Some(TypedActor(Akka.system).typedActorOf(TypedProps[RedisActor]()))
    Logger.info("Starting Wazza key-value store plugin")
  }

  override def onStop() = {
    Logger.info("Shuting down Wazza key-value store plugin")
    TypedActor(Akka.system).stop(_kvActor.get)
  }

  override def enabled = true
}

object KeyValueStorePlugin {

  def actor(implicit app: Application) = current.dbActor

  def current(implicit app: Application): KeyValueStorePlugin = app.plugin[KeyValueStorePlugin] match {
    case Some(plugin) => plugin
    case _ => throw new PlayException("Wazza KeyValueStorePlugin","Error occurred while initializing KeyValueStorePlugin")
  }
}


