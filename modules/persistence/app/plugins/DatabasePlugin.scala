package persistence.plugins

import play.api.{Plugin, Application}
import persistence.plugin.actors._
import akka.actor.TypedActor
import akka.actor.TypedProps
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api._

class DatabasePlugin(implicit app: Application) extends Plugin {

  private var _dbActor: Option[DatabaseActor] = None
  def dbActor = _dbActor.getOrElse(throw new RuntimeException("DatabasePlugin error: no plugin available?"))

  override def onStart() = {
    _dbActor = Some(TypedActor(Akka.system).typedActorOf(TypedProps[MongoActor]()))
    Logger.info("Starting Wazza database plugin")
  }

  override def onStop() = {
    Logger.info("Shuting down Wazza database plugin")
    TypedActor(Akka.system).stop(_dbActor.get)
  }

  override def enabled = true
}

object DatabasePlugin {

  def actor(implicit app: Application) = current.dbActor

  def current(implicit app: Application): DatabasePlugin = app.plugin[DatabasePlugin] match {
    case Some(plugin) => plugin
    case _ => throw new PlayException("Wazza DatabasePlugin","Error occurred while initializing DatabasePlugin")
  }
}

