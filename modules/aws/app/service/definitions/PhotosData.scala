package service.aws.definitions

import scala.util.{Try, Success, Failure}
import scala.concurrent._
import play.api.Play
import models.aws._
import service.aws.definitions._
import scala.util.{Try, Success, Failure}
import com.amazonaws.auth._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.mvc.MultipartFormData._
import java.io.File
import play.api.http.Status
import java.io.FileInputStream
import java.security.MessageDigest
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import com.github.nscala_time.time.Imports._

trait PhotosData {

  protected val PhotosBucket: Try[String] = Play.current.configuration.getConfig("aws") match {
      case Some(config) => Success(config.underlying.root.get("photosBucket").render.filter(_ != '"'))
      case _ => Failure(new Exception("AWS Credentials do not exist"))
  }

  protected object ExpirationDate{
    val Year = 2035;
    val Month = 12
    val Day = 25
  }

  protected def getAWSCredentials(): Try[Map[String, String]] = {
    Play.current.configuration.getConfig("aws") match {
      case Some(config) => {
        Success(Map(
          "accessKeyId" -> config.underlying.root.get("accessKeyId").render.filter(_ != '"'),
          "secretKey" -> config.underlying.root.get("secretKey").render.filter(_ != '"')
          )
        )
      }
      case _ => Failure(new Exception("AWS Credentials do not exist"))
    }
  }

  protected def getS3Client(bucketName: String): Try[AmazonS3Client] = {
    getAWSCredentials match {
      case Success(credentialData) => {
        val myCredentials = new BasicAWSCredentials(credentialData("accessKeyId"), credentialData("secretKey"))
        Success(new AmazonS3Client(myCredentials))
      }
      case Failure(failure) => Failure(failure)
    }
  }

}
