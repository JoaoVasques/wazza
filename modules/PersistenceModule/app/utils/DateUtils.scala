package persistence.utils

import org.joda.time.{DateTime, LocalDate, Days}
import scala.collection.immutable.StringOps
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.immutable.StringOps

object DateUtils {

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

  def buildDateFromString(dateStr: String): Date = {
    dateFormat.parse(dateStr)
  }

  def getDateFromString(dateStr: String): Date = {
    val ops = new StringOps(dateStr)
    new SimpleDateFormat("yyyy-MM-dd").parse(ops.take(ops.indexOf('T')))
  }

  def getNumberDaysBetweenDates(d1: Date, d2: Date): Int = {
    Days.daysBetween(new LocalDate(d1), new LocalDate(d2)).getDays()
  }

   def getNumberSecondsBetweenDates(d1: Date, d2: Date): Float = {
    (new LocalDate(d2).toDateTimeAtCurrentTime.getMillis - new LocalDate(d1).toDateTimeAtCurrentTime().getMillis) / 1000
  }

}
