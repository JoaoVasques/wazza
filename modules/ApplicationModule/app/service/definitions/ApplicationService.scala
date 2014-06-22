package service.application.definitions

import models.application._
import scala.util.Try
import scala.concurrent._

trait ApplicationService {

  protected lazy val ItemBatch = 10

  def insertApplication(companyName: String, application: WazzaApplication): Try[WazzaApplication]

  def deleteApplication(companyName: String, name: WazzaApplication): Try[Unit]

  def exists(companyName: String, name: String): Boolean

  def find(companyName: String, key: String): Option[WazzaApplication]

  def getApplicationyTypes: List[String]

  def getApplicationCountries(companyName: String, appName: String): List[String]

  def getApplicationCredentials(companyName: String, appName: String): Option[Credentials]

  /** Item operations **/

  def addItem(companyName: String, item: Item, applicationName: String): Try[Item]

  def getItem(companyName: String, itemId: String, applicationName: String): Option[Item]

  def getItems(companyName: String, applicationName: String, offset: Int = 0, projection: String = null): List[Item]

  def getItemsNotPurchased(companyName: String, applicationName: String, userId: String, limit: Int): List[Item]

  def itemExists(companyName: String, item: String, applicationName: String): Boolean

  def deleteItem(companyName: String, itemId: String, applicationName: String, imageName: String): Future[Unit]

  /** Virtual currency operations **/

  def addVirtualCurrency(companyName: String, currency: VirtualCurrency, applicationName: String): Try[VirtualCurrency]

  def deleteVirtualCurrency(companyName: String, currencyName: String, applicationName: String): Try[Unit]

  def getVirtualCurrency(companyName: String, currencyName: String, applicationName: String): Option[VirtualCurrency]

  def getVirtualCurrencies(companyName: String, applicationName: String): List[VirtualCurrency]

  def virtualCurrencyExists(companyName: String, currencyName: String, applicationName: String): Boolean

  /**
    General information about every entity in the system - companies and apps
  **/

  def getCompanies(): List[CompanyData]
}
