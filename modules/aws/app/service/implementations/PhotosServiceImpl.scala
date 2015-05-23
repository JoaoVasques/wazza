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
import java.time._
import java.util.Date

class PhotosServiceImpl extends PhotosService {

  private def generateFileName(file: File): String = {
    (new HexBinaryAdapter()).marshal(MessageDigest.getInstance("SHA-1").digest(file.getName.getBytes()))
  }

  private def generateS3ObjectURL(bucketName: String, fileName: String, s3Client: AmazonS3Client): String = {
    val expirationDate = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault())
      .withYear(ExpirationDate.Year)
      .withMonth(ExpirationDate.Month)
      .withDayOfMonth(ExpirationDate.Day)

    val request = new GeneratePresignedUrlRequest(bucketName, fileName, HttpMethod.GET)
    request.setExpiration(Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant()))
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

  def delete(imageName: String): Future[Unit] = {
    val promise = Promise[Unit]

    Future {
      PhotosBucket match {
        case Success(bucket) => {
          try {
            val s3Client = getS3Client(bucket).get
            val request = new DeleteObjectRequest(bucket, imageName)
            s3Client.deleteObject(request)
            promise.success()
          } catch {
              case err: AmazonServiceException if err.getStatusCode == Status.NOT_FOUND => promise.failure(new S3NotFound(bucket, imageName))
              case err: Throwable => promise.failure(new S3Failed(err))
          }
        }
        case Failure(failure) => promise.failure(failure)
      }
    }

    promise.future
  }
}
