package payments

import payments.paypal._
import com.tzavellas.sse.guice.ScalaModule

class PaymentsModule extends ScalaModule {
  def configure() {
    bind[PayPalService].to[PayPalServiceImpl]
  }
}

