package payments.paypal

import play.api.libs.json._
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import play.api.http.Status

class PayPalServiceImpl extends PayPalService {

  private val PAY_PAL_URL = "https://api.sandbox.paypal.com/v1/"

  def getAccessToken(clientID: String, secret: String): Future[String] = {
    val futureResult = WS.url(PAY_PAL_URL + "oauth2/token")
      .withHeaders("Content-type" -> "application/x-www-form-urlencoded")
      .withAuth(clientID, secret, WSAuthScheme.BASIC)
      .withFollowRedirects(true)
      .post("grant_type=client_credentials")

    futureResult map {response =>
      if(response.status == Status.OK) {
        println(response.json)
        (response.json \ "access_token").as[String]
      } else {
        null
      }
    }
  }

  def verifyPayment(accessToken: String, paymentID: String, amount: Double, currency: String): Future[Boolean] = {
    val verificationUrl = s"payments/payment/${paymentID}"
    val futureResult =  WS.url(PAY_PAL_URL + verificationUrl)
      .withHeaders("Content-type" -> "application/x-www-form-urlencoded")
      .withHeaders("Authorization" -> s"Bearer ${accessToken}")
      .get

    futureResult map {result =>
      println("VERIFICATION")
      println(result.status)
      println(result.json)
    }

    null
  }
}

