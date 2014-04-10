package service.recommendation.modules

import com.tzavellas.sse.guice.ScalaModule
import service.definitions.recommendation._
import service.implementations.recommendation._

class RecommendationModule extends ScalaModule {
  def configure() {
    bind[RecommendationService].to[PredictionIORecommendationService]
  }
}

