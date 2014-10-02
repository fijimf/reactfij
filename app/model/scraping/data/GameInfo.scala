package model.scraping.data

import java.util.TimeZone

import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{Imports, MongoClient}
import org.joda.time.{DateTimeZone, DateTime, LocalTime, LocalDate}
import play.api.Logger

case class GameInfo(
                     id: String,
                     conference: String,
                     gameState: String,
                     startDate: LocalDate,
                     startTime: LocalTime,
                     finalMessage: String,
                     gameStatus: String,
                     location: String,
                     contestName: String,
                     url: String,
                     scoreBreakdown: Seq[String],
                     home: GameTeam,
                     away: GameTeam,
                     tabsArray: Seq[Seq[GameLinks]]
                     ) {

  def gameId: Int = id.toInt

  def conferences: Seq[String] = conference.split(" ")

  def gameTime: DateTime = startDate.toDateTime(startTime, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New York")))


}

object GameInfo {
  def mergeGameInfo(client: MongoClient, g: GameInfo, seasonKey: String) = {
    val db = client("deepfij")
    val games = db("games")
    val teams = db("teams")

    val homeTeamKey = g.home.key
    val deptTwitter: Option[String] = g.home.social.twitter.headOption.flatMap(_.accounts.athleticDept)
    val sportTwitter: Option[String] = g.home.social.twitter.headOption.flatMap(_.accounts.sport)
    val awayTeamKey = g.away.key

    Logger.info("Merging game #"+g.gameId+" "+g.away.key+" @ "+g.home.key)

    for (
      h<-loadTeam(teams, homeTeamKey, deptTwitter, sportTwitter);
      a<-loadTeam(teams, awayTeamKey)
    ) {
      println("Processing");
    }

//    val q: Imports.MongoDBObject = new Imports.MongoDBObject(Map("_id" -> g.id.toInt))
//    val flag: Imports.MongoDBObject = new Imports.MongoDBObject(Map("multi" -> false, "upsert" -> true))
////    collection.update(q, g.toMongoObj(seasonKey), upsert = true, multi = false)
    //Logger.info(result)
  }

  def loadTeam(teams: casbah.MongoCollection, key: String, deptTwitter:Option[String], sportTwitter:Option[String]): Option[teams.T] = {
    val maybeT: Option[casbah.MongoCollection#T] = for (team <- teams.findOneByID(key)) yield team
    if (maybeT.isEmpty) Logger.info(key+" not found")
    maybeT.orElse()
    maybeT
  }
}

//TODO "status": {},
//TODO "alerts": {}
//TODO "champInfo": {},
