package controllers.api

import java.security.MessageDigest
import models.application.Credentials
import play.api._
import play.api.libs.Crypto
import play.api.mvc._

import play.api.cache._
import play.api.libs.json._
import play.api.data._
import play.api.mvc.Results._
import play.api.mvc.BodyParsers.parse
import com.google.inject._
import service.application.definitions.{ApplicationService}
import service.application.modules._
import service.aws.modules.AWSModule
import service.persistence.modules.PersistenceModule
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.binary.Hex

trait ApiSecurity { self: Controller =>

/**  val CompanyNameHeader = "CompanyName"
  val ApplicationNameHeader = "AppName"
  val MessageDigestHeader = "Digest"

  private val HashAlgorithm = "SHA-256"
  private val Encoding = "UTF-8"

  private  val applicationService = Guice.createInjector(
    new AppModule,
    new AWSModule,
    new PersistenceModule
  ).getInstance(classOf[ApplicationService])

  implicit val app: play.api.Application = play.api.Play.current


  def checkMessageValidity[A](
    request: Request[A],
    body: A,
    f: Request[A] => Result,
    credentials: Credentials
  ): Result = {

    def checkMessageIntegrity(messageDigest: String, content: String): Boolean = {
      val md = MessageDigest.getInstance(HashAlgorithm)
      md.update(content.getBytes(Encoding))
      Hex.encodeHexString(md.digest()) == messageDigest
    }

    body match {
      case b: JsValue => {
        try { 
          if(checkMessageIntegrity(request.headers.get("Digest").get, (b \ "content").as[String])) {
            f(request)
          } else {
            BadRequest
          }
        } catch {
          case e: Exception => {
            println(e.getMessage())
            BadRequest
          }
        }
      }
      case _ => BadRequest
    }
  }


  def ApiSecurityHandler[A]
    (p: BodyParser[A] = parse.anyContent)
    (f:  Request[A] => Result): Action[A] = Action(p){implicit request =>
    val companyName = request.headers.get(CompanyNameHeader).getOrElse(null)
    val applicationName = request.headers.get(ApplicationNameHeader).getOrElse(null)

    if(companyName == null || applicationName == null) {
      println("missing headers..")
      BadRequest
    } else {
      if(request.method == "GET") {
        f(request)
      } else {
        applicationService.getApplicationCredentials(companyName, applicationName) match {
          case Some(credentials) => {
            checkMessageValidity(request, request.body, f, credentials)
          }
          case None => BadRequest
        }
      }
    }
  }

  def ApiAuthenticationHandler[A]
    (p :BodyParser[A] = parse.anyContent)
    (f: String => Request[A] => Result): Action[A] = Action(p) {implicit request =>
    f("")(request)
  }
  * */
}

