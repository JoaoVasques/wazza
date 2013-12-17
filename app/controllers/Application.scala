package controllers

import play.api._
import play.api.mvc._
import com.google.inject._
import service._
// import service.editor._ just to show how imports from other modules are done

class Application @Inject()(translator: Translator) extends Controller {

  def index = Action {
    println(translator.translate("hello world"))
    Ok(views.html.index("Your new application is ready."))
  }

}
