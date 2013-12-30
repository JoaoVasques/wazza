package service.photos.definitions

import models.photos._
import com.mongodb.casbah.gridfs._
import java.io.File
import play.api.mvc.MultipartFormData._
import scala.util.Try

trait UploadPhotoService {

  def upload(filePart: FilePart[_]): Try[Unit]

  def getPhoto(fileName: String): Unit
}
