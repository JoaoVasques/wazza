package service.application.definitions

import models.application._
import scala.util.Try
import scala.concurrent._
import models.user.{CompanyData}

trait ApplicationService {

  protected lazy val ItemBatch = 10

  def insertApplication(companyName: String, application: WazzaApplication): Future[WazzaApplication]

  def deleteApplication(companyName: String, name: WazzaApplication): Future[Unit]

  def exists(companyName: String, name: String): Future[Boolean]

  def find(companyName: String, key: String): Future[Option[WazzaApplication]]

  def getApplicationyTypes: List[String]

  def getApplicationCountries(companyName: String, appName: String): List[String]

  def getApplicationCredentials(companyName: String, appName: String): Future[Option[Credentials]]

  /** Item operations **/

  def addItem(companyName: String, item: Item, applicationName: String): Future[Unit]

  def getItem(companyName: String, itemId: String, applicationName: String): Future[Option[Item]]

  def getItems(
    companyName: String,
    applicationName: String,
    offset: Int = 0,
    projection: String = null
  ): Future[List[Item]]

  def getItemsNotPurchased(
    companyName: String,
    applicationName: String,
    userId: String,
    limit: Int
  ): Future[List[Item]]

  def itemExists(companyName: String, item: String, applicationName: String): Future[Boolean]

  def deleteItem(companyName: String, itemId: String, applicationName: String, imageName: String): Future[Unit]

  /** Virtual currency operations **/

  def addVirtualCurrency(companyName: String, currency: VirtualCurrency, applicationName: String): Future[Unit]

  def deleteVirtualCurrency(companyName: String, currencyName: String, applicationName: String): Future[Unit]

  def getVirtualCurrency(
    companyName: String,
    currencyName: String,
    applicationName: String
  ): Future[Option[VirtualCurrency]]

  def getVirtualCurrencies(companyName: String, applicationName: String): Future[List[VirtualCurrency]]

  def virtualCurrencyExists(companyName: String, currencyName: String, applicationName: String): Future[Boolean]

  /**
    General information about every entity in the system - companies and apps
  **/

  def getCompanies(): Future[List[CompanyData]]
}

