package service.aws.definitions

import scala.util.{Try, Success, Failure}
import scala.concurrent._

trait UploadFileService {

  def upload(): Future[Try[String]]

}
