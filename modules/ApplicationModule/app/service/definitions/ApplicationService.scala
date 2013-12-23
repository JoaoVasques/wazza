package service.application.definitions

import models.application._

trait ApplicationService {
	
	def insertApplication(application: WazzaApplication): Boolean		

	def deleteApplication(name: String): Unit

	def exists(name: String): Boolean

	def findBy(attribute: String, key: String): List[WazzaApplication]

	def addItem(item: Item, applicationName: String): Boolean	
}
