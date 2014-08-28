package model.scraping

import scrapers._
import actors.{DailyScoreboardActor, GameInfoActor}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.joda.time.LocalDate

object ActorTest {
  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
 TeamListScraper.loadTeamList()
 TeamPageScraper.loadPage("villanova")
//
//    val system: ActorSystem = ActorSystem("deepfij")
//    val sa: ActorRef = system.actorOf(Props[DailyScoreboardActor], "scoreboard")
//    val ga: ActorRef = system.actorOf(Props[GameInfoActor], "gameinfo")
//    sa ! new LocalDate(2014, 2, 12)
  }
}
