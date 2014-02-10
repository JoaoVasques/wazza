package service.aws.implementations

import service.aws.definitions._
import scala.util.{Try, Success, Failure}
import play.api.Play
import com.amazonaws.auth._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.mvc.MultipartFormData._
import java.io.File
import models.aws._
import play.api.http.Status
import java.io.FileInputStream
import java.security.MessageDigest
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import com.github.nscala_time.time.Imports._

class UploadFileServiceImpl extends UploadFileService {

  private object ExpirationDate{
    val Year = 2035;
    val Month = 12
    val Day = 25
  }

  private def getAWSCredentials(): Try[Map[String, String]] = {
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

  private def getS3Client(bucketName: String): Try[AmazonS3Client] = {
    getAWSCredentials match {
      case Success(credentialData) => {
        val myCredentials = new BasicAWSCredentials(credentialData("accessKeyId"), credentialData("secretKey"))
        Success(new AmazonS3Client(myCredentials))
      }
      case Failure(failure) => Failure(failure)
    }
  }

  private def generateFileName(file: File): String = {
    (new HexBinaryAdapter()).marshal(MessageDigest.getInstance("SHA-1").digest(file.getName.getBytes()))
  }

  private def generateS3ObjectURL(bucketName: String, fileName: String, s3Client: AmazonS3Client): String = {
    val expirationDate = (new DateTime).withYear(ExpirationDate.Year)
        .withMonthOfYear(ExpirationDate.Month)
        .withDayOfMonth(ExpirationDate.Day)

    val request = new GeneratePresignedUrlRequest(bucketName, fileName, HttpMethod.GET)
    request.setExpiration(expirationDate.toDate)
    s3Client.generatePresignedUrl(request).toString
  }

  def upload(file: File): Future[UploadPhotoResult] = {
    val promise = Promise[UploadPhotoResult]

    Future {
      PhotosBucket match {
        case Success(bucket) => {
          try {
              val s3Client = getS3Client(bucket).get
              val fileName = generateFileName(file)
              val request = new PutObjectRequest(bucket,fileName, new FileInputStream(file), new ObjectMetadata())
              request.withCannedAcl(CannedAccessControlList.PublicRead)
              s3Client.putObject(request)
              promise.success(new UploadPhotoResult(fileName, generateS3ObjectURL(bucket, fileName, s3Client)))
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
