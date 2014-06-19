package service.analytics.modules

import com.tzavellas.sse.guice.ScalaModule
import service.analytics.definitions.AnalyticsService
import service.analytics.implementations.AnalyticsServiceImpl

class AnalyticsModule extends ScalaModule {
  def configure() {
    bind[AnalyticsService].to[AnalyticsServiceImpl]
  }
}
