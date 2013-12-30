package service.photos.implementations

import service.photos.definitions._
import models.photos._
import play.api.mvc.MultipartFormData._
import java.io.File
import java.io.FileInputStream
import play.api.libs.Files._
import scala.util.{Try, Success, Failure}

class UploadPhotoServiceImpl extends UploadPhotoService {

  private val storage = Photo.photosStorage

  def upload(filePart: FilePart[_]): Try[Unit] = {
    filePart.ref match {
      case TemporaryFile(file) => {
        val input = new FileInputStream(file)
        storage(input){ fh =>
          fh.filename = filePart.filename
          fh.contentType = filePart.contentType.get
        }
        new Success
      }
      case _ => {
        new Failure(null)
      }
    }
  }

  def getPhoto(fileName: String): Unit = {}
}
