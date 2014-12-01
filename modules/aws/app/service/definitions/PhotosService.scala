package service.aws.definitions

import scala.util.{Try, Success, Failure}
import scala.concurrent._
import play.api.Play
import java.io.File
import models.aws._

trait PhotosService extends PhotosData {

  def upload(file: File): Future[UploadPhotoResult]

  def delete(name: String): Future[Unit]

}
