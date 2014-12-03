package model.scraping

import org.joda.time.LocalTime

import scala.concurrent.Future


case class LoadStep(stepName:String, stepKey:String, start:LocalTime, end:Option[LocalTime], ok:Boolean, message:Option[String]) {

}

