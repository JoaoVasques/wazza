package service.aws.implementations

import service.aws.definitions._
import scala.util.{Try, Success, Failure}
import play.api.Play
import com.amazonaws.auth._
import com.amazonaws.services.s3._
import scala.concurrent._
import ExecutionContext.Implicits.global

class UploadFileServiceImpl extends UploadFileService {

  private def getAWSCredentials(): Try[Map[String, String]] = {
    Play.current.configuration.getConfig("aws") match {
      case Some(config) => {
        Success(Map(
          "accessKeyId" -> config.underlying.root.get("accessKeyId").render,
          "secretKey" -> config.underlying.root.get("secretKey").render
          )
        )
      }
      case _ => Failure(new Exception("AWS Credentials do not exist"))
    }
  }

  private def getS3Client(bucketName: String, key: String): Try[AmazonS3Client] = {
    getAWSCredentials match {
      case Success(credentialData) => {
        val myCredentials = new BasicAWSCredentials(credentialData("myAccessKeyID"), credentialData("mySecretKey"))
        Success(new AmazonS3Client(myCredentials))
      }
      case Failure(failure) => Failure(failure)
    }
  }

  def upload(): Future[Try[String]] = {
    getAWSCredentials match {
      case Success(credentials) => {
        println(credentials)
        null
      }
      case Failure(_) => null
    }
  }
  
}
