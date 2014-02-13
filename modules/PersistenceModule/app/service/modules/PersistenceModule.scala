package service.persistence.modules

import com.tzavellas.sse.guice.ScalaModule
import service.persistence.definitions._
import service.persistence.implementations._

class PersistenceModule extends ScalaModule {
  def configure() {
    bind[DatabaseService].to[MongoDatabaseService]
  }
}
