package model.scraping.actors

import akka.actor.Actor
import model.scraping.data.GameInfo
import org.joda.time.{DateTime, LocalDate}

class GameCoordinator extends Actor{
  case class Game(officialId:Int, date:LocalDate, time:DateTime, homeTeamKey:String, awayTeamKey:String)
  val gameData:Map[Int,Game] = Map.empty
  override def receive: Receive = {
    case gi:GameInfo => {


    }
  }
}
