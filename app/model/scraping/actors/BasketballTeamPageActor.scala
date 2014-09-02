package model.scraping.actors

import akka.actor.Actor
import model.scraping.scrapers.{TeamPageScraper, BasketballTeamPageScraper, BoxScoreScraper}
import play.api.Logger

case class BasketballTeamPageReq(url:String)
case class BasketballTeamPageResp(url:String,map:Map[String,Any] )

case class TeamPageReq(url:String)
case class TeamPageResp(url:String,map:Map[String,Any] )

class BasketballTeamPageActor extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def receive: Receive = {
    case BasketballTeamPageReq(url) =>
      Logger.info("Loading "+url)
      BasketballTeamPageScraper.loadPage(url).map((map: Map[String, Any]) => {
         sender() ! BasketballTeamPageResp(url, map)
      })
    case _ => Logger.info("Unexpected message")
  }
}

class TeamPageActor extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def receive: Receive = {
    case TeamPageReq(url) =>
      Logger.info("Loading "+url)
      TeamPageScraper.loadPage(url).map((map: Map[String, Any]) => {
         sender() ! TeamPageResp(url, map)
      })
    case _ => Logger.info("Unexpected message")
  }
}
