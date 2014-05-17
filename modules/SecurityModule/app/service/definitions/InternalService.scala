package service.security.definitions

import models.security.CompanyData

trait InternalService {

  def addCompany(companyName: String)

  def addApplication(companyName: String, applicationName: String)

  def getCompanies(): List[CompanyData]
}
