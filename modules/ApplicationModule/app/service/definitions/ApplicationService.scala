package service.application.definitions

import models.application._
import scala.util.Try

trait ApplicationService {
  
  def insertApplication(application: WazzaApplication): Try[WazzaApplication]

  def deleteApplication(name: String): Try[WazzaApplication]

  def exists(name: String): Boolean

  def find(key: String): Option[WazzaApplication]

  def getApplicationyTypes: Map[String, String]

  /** Item operations **/

  def addItem(item: Item, applicationName: String): Try[Item]

  def getItem(itemId: String, applicationName: String): Option[Item]

  def itemExists(itemId: String, applicationName: String): Boolean

  def deleteItem(itemId: String, applicationName: String): Try[Item]

}
