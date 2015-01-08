package service.analytics.definitions

import java.util.Date
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

trait AnalyticsService {

  def getTopTenItems(companyName: String, applicationName: String, start: Date, end: Date): Future[JsArray]

  def getARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalARPU(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getTotalAverageRevenuePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getTotalAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getAveragePurchasesUser(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getLifeTimeValue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getAverageTimeFirstPurchase(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getAverageTimeBetweenPurchases(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getNumberPayingCustomers(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]

  def getTotalAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsValue]

  def getAveragePurchasePerSession(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    platforms: List[String]
  ): Future[JsArray]
}

