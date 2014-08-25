package model.scraping.actors

import akka.actor.Actor
import model.scraping.scrapers.BoxScoreScraper
import play.api.Logger

class BoxScoreActor extends Actor {
  override def receive: Receive = {
    case url: String =>
      Logger.info("Loading "+url)
      BoxScoreScraper.loadBoxScore(url)
    case _ => Logger.info("Unexpected message")
  }
}
