package service.implementations

import service.definitions.ApplicationService
import models.WazzaApplication
import com.mongodb.casbah.Imports._

class ApplicationServiceImpl extends ApplicationService {
	
	private val dao = WazzaApplication.getDAO

	def insertApplication(application: WazzaApplication): Boolean = {

		if(! exists(application.name)){
			dao.insert(application)
			true
		} else {
			false
		}
	}

	def deleteApplication(name: String): Unit = {
		if(exists(name)){
			val application = findBy("name", name)
			dao.remove(application.head)
		}
	}

	def exists(name: String): Boolean = {
		! dao.findOne(MongoDBObject("name" -> name)).isEmpty
	}

	def findBy(attribute: String, key: String): List[WazzaApplication] = {
		dao.find(MongoDBObject(attribute -> key)).toList
	}
}
