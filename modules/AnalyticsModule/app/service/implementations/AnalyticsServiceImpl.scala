package service.analytics.implementations

import models.user.PurchaseInfo
import org.bson.BSONObject
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.util.Failure
import service.analytics.definitions.AnalyticsService
import java.util.Date
import play.api.libs.json.JsArray
import scala.concurrent._
import play.api.Play
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.hadoop.conf.Configuration
import org.bson.BSONObject
import org.bson.BasicBSONObject

class AnalyticsServiceImpl extends AnalyticsService {

  private lazy val context = new SparkContext("local", "Wazza Analytics")

  private val configuration = new HashMap[String, Configuration] with SynchronizedMap[String, Configuration]

  private def generateConfigurationName(companyName: String, applicationName: String, analyticsType: String) = {
    s"companyName-$analyticsType-$applicationName"
  }

  private def addConfiguration(companyName: String, applicationName: String, analyticsType: String) = {
    def getMongoUri(): String = {
      Play.current.configuration.getConfig("mongodb.dev") match {
        case Some(config) => {
          config.underlying.root.get("uri").render.filter(_ != '"')
        }
        case _ => null
      }
    }

    val inputUri = getMongoUri + "." + PurchaseInfo.getCollection(companyName, applicationName)
    val outputUri = getMongoUri + "." + generateConfigurationName(companyName, applicationName, analyticsType)
    val config = new Configuration
    config.set("mongo.input.uri", inputUri)
    config.set("mongo.output.uri", outputUri)

    configuration.synchronized {
      configuration.put(generateConfigurationName(companyName, applicationName, analyticsType), config)
    }
  }

  private def getConfiguration(companyName: String, applicationName: String, analyticsType: String): Configuration = {
    val confName = generateConfigurationName(companyName, applicationName, analyticsType)
    configuration.synchronized {
      configuration.get(confName) match {
        case Some(c) => c
        case None => {
          addConfiguration(companyName, applicationName, analyticsType)
          getConfiguration(companyName, applicationName, analyticsType)
        }
      }
    }
  }

  def getTopTenItems(companyName: String, applicationName: String): Future[JsArray] = {
    null
  }

  def getTotalRevenue(companyName: String, applicationName: String, start: Date, end: Date): Future[Double] = {
    val config = this.getConfiguration(companyName, applicationName, "totalRevenue")
    val mongoRDD = context.newAPIHadoopRDD(
      config,
      classOf[com.mongodb.hadoop.MongoInputFormat],
      classOf[Object],
      classOf[BSONObject]
    )
    null
  }

  def getAverageRevenueOnTimeRange(
    companyName: String,
    applicationName: String,
    start: Date,
    end: Date
  ): Future[Double] = {
    null
  }

}

