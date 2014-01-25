package service.application.definitions

import models.application._
import scala.util.Try

trait ApplicationService {

  def insertApplication(application: WazzaApplication): Try[WazzaApplication]

  def deleteApplication(name: String): Try[WazzaApplication]

  def exists(name: String): Boolean

  def find(key: String): Option[WazzaApplication]

  def getApplicationyTypes: List[String]

  /** Item operations **/

  def addItem(item: Item, applicationName: String): Try[Item]

  def getItem(itemId: String, applicationName: String): Option[Item]

  def getItems(applicationName: String, skip: Int): List[Item]

  def itemExists(keyValue: String, applicationName: String, key: String = "name"): Boolean

  def deleteItem(itemId: String, applicationName: String): Try[Unit]

  /** Virtual currency operations **/

  def addVirtualCurrency(currency: VirtualCurrency, applicationName: String): Try[VirtualCurrency]

  def deleteVirtualCurrency(currencyName: String, applicationName: String): Try[Unit]

  def getVirtualCurrency(currencyName: String, applicationName: String): Option[VirtualCurrency]

  def getVirtualCurrencies(applicationName: String): List[VirtualCurrency]

  def virtualCurrencyExists(currencyName: String, applicationName: String): Boolean

}
