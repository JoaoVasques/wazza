package service

import com.tzavellas.sse.guice.ScalaModule

class ProdModule extends ScalaModule {
  def configure() {
    bind[Translator].to[FrenchTranslatorImpl]
  }
}
