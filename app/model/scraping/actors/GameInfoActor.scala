package model.scraping.actors

import akka.actor.{Props, Actor}
import model.scraping.GameInfoScraper
import model.scraping.GameInfoScraper.GameInfo
import play.api.Logger


class GameInfoActor extends Actor {
  private val bsActor = context.actorOf(Props[BoxScoreActor])

  def gameUrl(scoreboardGame: String): String = {
    scoreboardGame.replace( """/sites/default/files/data""", """http://data.ncaa.com/jsonp""")
  }

  override def receive: Receive = {
    case url: String =>
      Logger.info("Loading " + url)
      GameInfoScraper.loadGameUrl(url, (info: GameInfo) => {
        info.tabsArray.seq.foreach(_.foreach(gi => {
          if (gi.linkType == "boxscore") {
            bsActor ! gameUrl(gi.file)
          }
        }))
      })

    case _ => Logger.info("Unexpected message")
  }
}