package service.application.definitions

import models.application._
import scala.util.Try
import scala.concurrent._

trait ApplicationService {

  protected lazy val ItemBatch = 10

  def insertApplication(application: WazzaApplication): Try[WazzaApplication]

  def deleteApplication(name: WazzaApplication): Try[Unit]

  def exists(name: String): Boolean

  def find(key: String): Option[WazzaApplication]

  def getApplicationyTypes: List[String]

  def getApplicationCountries(appName: String): List[String]

  def getApplicationCredentials(appName: String): Option[Credentials]

  /** Item operations **/

  def addItem(item: Item, applicationName: String): Try[Item]

  def getItem(itemId: String, applicationName: String): Option[Item]

  def getItems(applicationName: String, offset: Int = 0, projection: String = null): List[Item]

  def itemExists(item: String, applicationName: String): Boolean

  def deleteItem(itemId: String, applicationName: String, imageName: String): Future[Unit]

  /** Virtual currency operations **/

  def addVirtualCurrency(currency: VirtualCurrency, applicationName: String): Try[VirtualCurrency]

  def deleteVirtualCurrency(currencyName: String, applicationName: String): Try[Unit]

  def getVirtualCurrency(currencyName: String, applicationName: String): Option[VirtualCurrency]

  def getVirtualCurrencies(applicationName: String): List[VirtualCurrency]

  def virtualCurrencyExists(currencyName: String, applicationName: String): Boolean

}