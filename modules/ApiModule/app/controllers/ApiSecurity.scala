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

  /**
    Used in all the API calls.
    Steps:
     1- Gets secret given app name
     2- deciphers message, hashes it and compares with digest header
     3- proceed if everything is ok (missing timestamp for freshness)
  **/

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
    //println(s"HEADERS ${request.headers}")
    request.headers.get(ApplicationNameHeader) match {
      case Some(name) => {
        if(request.method == "GET") {
          f(request)
        } else {
          applicationService.getApplicationCredentials(name) match {
            case Some(credentials) => {
              checkMessageValidity(request, request.body, f, credentials)
            }
            case None => BadRequest
          }
        }
      }
      case None => {
        println("No application header")
        BadRequest
      }
    }
  }

  /**
    Used on the 3-way handshake protocol for generating a session key
    NOTE: To be implemented later
  **/
  def ApiAuthenticationHandler[A]
    (p :BodyParser[A] = parse.anyContent)
    (f: String => Request[A] => Result): Action[A] = Action(p) {implicit request =>
    f("")(request)
  }
}

