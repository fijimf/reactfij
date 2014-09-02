package model.scraping.actors

import akka.actor.{ActorRef, Actor, Props}
import model.scraping.data.{BoxScore, GameInfo}
import model.scraping.scrapers.GameInfoScraper
import play.api.Logger

case class GameBuilder(key:String, gameInfo:Option[GameInfo], boxScore:Option[BoxScore])


class GameInfoActor extends Actor {
  private val bsActor = context.actorOf(Props[BoxScoreActor])
  private var results = Map.empty[String,(ActorRef, GameBuilder)]

  def gameUrl(scoreboardGame: String): String = {
    scoreboardGame.replace( """/sites/default/files/data""", """http://data.ncaa.com/jsonp""")
  }

  override def receive: Receive = {
    case url: String =>
      Logger.info("Loading " + url)
      val pathParts: Array[String] = url.split("/")
      val gameKey =  pathParts(7)+ pathParts(8)+ pathParts(9)+":"+ pathParts(10)
      GameInfoScraper.loadGameUrl(url, (info: GameInfo) => {
        info.tabsArray.seq.foreach(_.foreach(gi => {
          if (gi.linkType == "boxscore") {
            bsActor ! gameUrl(gi.file)
          }
        }))
      })
    case score:BoxScore =>

    case _ => Logger.info("Unexpected message")
  }
}
