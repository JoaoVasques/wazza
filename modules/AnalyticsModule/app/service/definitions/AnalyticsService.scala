package service.analytics.definitions

import java.util.Date
import play.api.libs.json.JsArray
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

trait AnalyticsService {

  def calculateTopTenItems(companyName: String, applicationName: String, start: Date, end: Date)

  def calculateTotalRevenue(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  )

  def getTopTenItems(companyName: String, applicationName: String, start: Date, end: Date): Future[JsArray]

  def getTotalRevenue(companyName: String, applicationName: String, start: Date, end: Date): Future[Double]
}

