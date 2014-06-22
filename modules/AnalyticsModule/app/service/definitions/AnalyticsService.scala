package service.analytics.definitions

import java.util.Date
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

trait AnalyticsService {

  def calculateTopItems(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date,
    limit: Int
  ): Future[JsValue]

  def calculateTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue]

  def calculateAverageSessionLength(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[JsValue]

  def getTopTenItems(companyName: String, applicationName: String, start: Date, end: Date): Future[JsArray]

  def getTotalRevenue(companyName: String, applicationName: String, start: Date, end: Date): Future[JsArray]
}

