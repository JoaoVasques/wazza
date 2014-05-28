package service.security.implementations

import com.google.inject._
import play.api.libs.json.JsValue
import scala.util.Success
import scala.util.Try
import service.persistence.definitions._
import service.security.definitions.InternalService
import models.security._
import play.api.libs.json.Json

class InternalServiceImpl @Inject()(
  databaseService: DatabaseService
) extends InternalService {

  def addCompany(companyName: String): Try[Unit] = {
    if(!companyExists(companyName)) {
      val data = new CompanyData(companyName, List[String]())
      databaseService.insert(
        CompanyData.Collection,
        Json.toJson(data)
      )
    } else {
      new Success
    }
  }

  def addApplication(companyName: String, applicationName: String) = {
    if(!applicationExists(companyName, applicationName)) {
      databaseService.addElementToArray[String](
        CompanyData.Collection,
        CompanyData.Key,
        companyName,
        CompanyData.Apps,
        applicationName
      )
    }
  }

  def getCompanies(): List[CompanyData] = {
    databaseService.getCollectionElements(CompanyData.Collection) map {el =>
      new CompanyData(
        (el \ "name").as[String],
        (el \ "apps").as[List[String]]
      )
    }
    List(new CompanyData("CompanyTest", List("RecTestApp"))) //TODO dummy
  }

  private def companyExists(companyName: String): Boolean = {
    databaseService.exists(
      CompanyData.Collection,
      CompanyData.Key,
      companyName
    )
  }

  private def applicationExists(companyName: String, applicationName: String): Boolean = {
    databaseService.getElementsOfArray(
      CompanyData.Collection,
      CompanyData.Key,
      companyName,
      CompanyData.Apps,
      None
    ).toList.find((app: JsValue) => {
      //TODO
      println("app : " + app)
      true
    }) match {
      case Some(_) => true
      case None => false
    }
  }
}
