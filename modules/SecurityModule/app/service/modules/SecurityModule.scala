package service.security.modules

import com.tzavellas.sse.guice.ScalaModule
import service.security.definitions._
import service.security.implementations._

class SecurityModule extends ScalaModule {
  def configure() {
    bind[SecretGeneratorService].to[SecretGeneratorServiceImpl]
    bind[TokenManagerService].to[TokenManagerServiceImpl]
    bind[TokenStoreService].to[TokenStoreServiceCacheImpl]
    bind[RedirectionTableService].to[RedirectionTableServiceImpl]
  }
}
