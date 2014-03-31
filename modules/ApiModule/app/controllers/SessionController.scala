package controllers.api

import com.google.inject._
import play.api._
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

class SessionController @Inject()(

) extends Controller {

  def updateSession = Action(parse.json) {implicit request =>
    println("update session")
    val content = (request.body \ "content").as[String].replace("\\", "")
    println(content)
    Ok
  }
}

