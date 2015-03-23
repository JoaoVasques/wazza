package payments.paypal

import play.api.libs.json._
import scala.concurrent._

trait PayPalService {

  def getAccessToken(clientId: String, secret: String): Future[String]

  def verifyPayment(accessToken: String, paymentID: String, amount: Double, currency: String): Future[Boolean]
}

