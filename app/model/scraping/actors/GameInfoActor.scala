package model.scraping.actors

import akka.actor.Actor
import model.scraping.GameInfoScraper
import play.api.Logger

class GameInfoActor extends Actor {
  override def receive: Receive = {
    case url: String =>
      Logger.info("Loading "+url)
      GameInfoScraper.loadGameUrl(url)
    case _ => Logger.info("Unexpected message")
  }
}
