package service

import com.tzavellas.sse.guice.ScalaModule

class DevModule extends ScalaModule {
  def configure() {
    bind[Translator].to[FakeTranslator]
  }
}