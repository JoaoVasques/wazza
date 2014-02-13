package models.aws

import play.api.libs.json._

case class UploadPhotoResult(
  fileName: String,
  s3Url: String
) {

  def toJson(): JsValue = {
    Json.obj("fileName" -> this.fileName, "url" -> this.s3Url)
  }
}
