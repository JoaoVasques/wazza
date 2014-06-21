package service.security.definitions

import models.security.CompanyData
import scala.util.Try

trait InternalService {

  def addCompany(companyName: String): Try[Unit]

  def addApplication(companyName: String, applicationName: String)

  def getCompanies(): List[CompanyData]
}
