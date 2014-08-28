package model.scraping.actors

import akka.actor._
import model.scraping.data.Scoreboards
import model.scraping.scrapers.DailyScoreboardScraper
import org.joda.time.LocalDate
import play.api.Logger

class DailyScoreboardActor extends Actor {

  private val giActor: ActorRef = context.actorOf(Props[GameInfoActor])


  def gameUrl(scoreboardGame: String): String = {
    scoreboardGame.replace( """/sites/default/files/data""", """http://data.ncaa.com/jsonp""")
  }

  override def receive: Receive = {
    case d: LocalDate =>
      Logger.info("Received date " + d)
      DailyScoreboardScraper.loadDate(d, (s: Scoreboards) => {
        Logger.info("Processing scoreboards.")
        s.scoreboard.foreach(t => {
          t.games.foreach(g => {
            Logger.info(g.toString)
            giActor ! gameUrl(g)
          })
        })
      })
    case _ => Logger.info("Unexpected message")
  }
}

