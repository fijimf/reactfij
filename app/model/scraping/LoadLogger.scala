package model.scraping

import akka.actor.{ActorRef, Actor}
import org.joda.time.LocalTime

case class StepResult(start:LocalTime, end:Option[LocalTime], result:Either[Throwable,Option[String]]) {
  def update(m: String): StepResult = {
    copy(end = Some(new LocalTime()), result = Right(Some(m)))
  }

  def update(t: Throwable): StepResult = {
    copy(end = Some(new LocalTime()), result = Left(t))
  }
}

object StepResult {
  def apply():StepResult ={
     StepResult(new LocalTime(), None, Right(None))
  }
}

case class StepKey(loadKey:String, stepName:String, stepKey:String)

class LoadLogger extends Actor {

    private var log:Map[StepKey, StepResult ]=Map.empty

    override def receive: Actor.Receive = {
      case (k:StepKey, m:String) => log = log + (k->log.get(k).map(_.update(m)).getOrElse(StepResult()))
      case (k:StepKey, t:Throwable) => log = log + (k->log.get(k).map(_.update(t)).getOrElse(StepResult().update(t)))
      case ShowLoadResults(s) => {
        val sorted: List[(StepKey, StepResult)] = log.filterKeys(_.loadKey==s).toList.sortBy(_.toString)
        sender() ! sorted
      }
    }

}

case class ShowLoadResults(loadId: String) {

}
