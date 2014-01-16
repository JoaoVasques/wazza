package service.aws.implementations

import service.aws.definitions._
import scala.util.{Try, Success, Failure}
import play.api.Play
import com.amazonaws.auth._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.AmazonServiceException
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.mvc.MultipartFormData._
import java.io.File
import models.aws._
import play.api.http.Status
import java.io.FileInputStream

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

  private def getS3Client(bucketName: String): Try[AmazonS3Client] = {
    getAWSCredentials match {
      case Success(credentialData) => {
        val myCredentials = new BasicAWSCredentials(credentialData("accessKeyId"), credentialData("secretKey"))
        Success(new AmazonS3Client(myCredentials))
      }
      case Failure(failure) => Failure(failure)
    }
  }

  def upload(file: File): Future[PutObjectResult] = {
    val promise = Promise[PutObjectResult]

    Future {
      PhotosBucket match {
        case Success(bucket) => {
          try {
              val s3Client = getS3Client(bucket).get
              s3Client.putObject(new PutObjectRequest(bucket, file.getName, new FileInputStream(file), new ObjectMetadata()))
            } catch {
              case err: AmazonServiceException if err.getStatusCode == Status.NOT_FOUND => promise.failure(new S3NotFound(bucket, file.getName))
              case err: Throwable => promise.failure(new S3Failed(err))
            }
        }
        case Failure(failure) => promise.failure(failure)
      }
    }

    promise.future
  }
}
