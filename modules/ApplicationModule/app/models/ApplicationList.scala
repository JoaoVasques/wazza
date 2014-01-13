package models.application

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

@Salat
trait ApplicationList {
  def attributeName: String
  def elementId : String 
}
