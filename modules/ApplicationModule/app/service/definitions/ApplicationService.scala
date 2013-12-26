package service.application.definitions

import models.application._

trait ApplicationService {
  
  def insertApplication(application: WazzaApplication): Unit    

  def deleteApplication(name: String): Unit

  def exists(name: String): Boolean

  def find(key: String): Option[WazzaApplication]

  /** Item operations **/

  def addItem(item: Item, applicationName: String): Unit  

  def getItem(itemId: String, applicationName: String): Option[Item]

  def itemExists(itemId: String, applicationName: String): Boolean

  def deleteItem(itemId: String, applicationName: String): Unit

}
