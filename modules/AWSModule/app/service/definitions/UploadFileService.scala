package service.aws.definitions

import scala.util.{Try, Success, Failure}
import scala.concurrent._
import play.api.Play
import java.io.File
import models.aws._

trait UploadFileService {

  protected val PhotosBucket: Try[String] = Play.current.configuration.getConfig("aws") match {
      case Some(config) => Success(config.underlying.root.get("photosBucket").render.filter(_ != '"'))
      case _ => Failure(new Exception("AWS Credentials do not exist"))
  }

  def upload(file: File): Future[UploadPhotoResult]

}
