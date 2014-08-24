package model.scraping

import akka.actor.{Props, ActorRef, ActorSystem}
import model.scraping.actors.{DailyScoreboardActor, GameInfoActor}
import org.joda.time.LocalDate

object ActorTest {
  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val system: ActorSystem = ActorSystem("deepfij")
    val sa: ActorRef = system.actorOf(Props[DailyScoreboardActor], "scoreboard")
    val ga: ActorRef = system.actorOf(Props[GameInfoActor], "gameinfo")
    sa ! new LocalDate(2014, 2, 12)
  }
}
