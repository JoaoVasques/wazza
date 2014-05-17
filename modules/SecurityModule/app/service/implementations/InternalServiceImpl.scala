package service.security.implementations

import com.google.inject._
import service.persistence.definitions._
import service.security.definitions.InternalService
import models.security._
import play.api.libs.json.Json

class InternalServiceImpl @Inject()(
  databaseService: DatabaseService
) extends InternalService {

  def addCompany(companyName: String) = {
    val data = new CompanyData(companyName, List[String]())
    databaseService.insert(
      CompanyData.collection,
      Json.toJson(data)
    )
  }

  def addApplication(companyName: String, applicationName: String) = {
    //TODO
  }

  def getCompanies(): List[CompanyData] = {
    //TODO
    List(new CompanyData("CompanyTest", List("RecTestApp")))
  }

}
