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

    Logger.info("Merging game #" + g.gameId + " " + g.away.key + " @ " + g.home.key)
    val h = loadTeam(teams, g.home)
    val a = loadTeam(teams, g.away)

    //    println(h)
    //    println(a)
    //    val q: Imports.MongoDBObject = new Imports.MongoDBObject(Map("_id" -> g.id.toInt))
    //    val flag: Imports.MongoDBObject = new Imports.MongoDBObject(Map("multi" -> false, "upsert" -> true))
    ////    collection.update(q, g.toMongoObj(seasonKey), upsert = true, multi = false)
    //Logger.info(result)
  }

  def loadTeam(teams: casbah.MongoCollection, gameTeam: GameTeam): Option[teams.T] = {
    val key: String = gameTeam.key
    val twitterTeam: Option[String] = gameTeam.social.twitter.flatMap(_.accounts.sport)
    val twitterSchool: Option[String] = gameTeam.social.twitter.flatMap(_.accounts.athleticDept)
    val facebook: Option[String] = gameTeam.social.facebook.flatMap(_.accounts.athleticDept)
    val optTeam = teams.findOneByID(key).
                  orElse(teams.findOneByID(key.replaceFirst("st-", "saint-"))).
                  orElse(teams.findOneByID(key.replaceFirst("-st", "-state"))).
                  orElse(teams.findOneByID(key.replaceFirst("saint-", "st-"))).
                  orElse(teams.findOneByID(key.replaceFirst("-state", "-st"))).
                  orElse(teams.findOneByID(key.replaceFirst("ne", "northeast"))).
                  orElse(teams.findOneByID(key.replaceFirst("nw", "northwest"))).
                  orElse(teams.findOneByID(key.replaceFirst("se", "southeast"))).
                  orElse(teams.findOneByID(key.replaceFirst("sw", "southwest"))).
                  orElse(teams.findOneByID(key.replaceFirst("northeast", "ne"))).
                  orElse(teams.findOneByID(key.replaceFirst("northwest", "nw"))).
                  orElse(teams.findOneByID(key.replaceFirst("southeast", "se"))).
                  orElse(teams.findOneByID(key.replaceFirst("southwest", "sw"))).
                  orElse(teams.findOneByID(key.replaceFirst("ill", "illinois"))).
                  orElse(teams.findOneByID(key.replaceFirst("illinois", "ill"))).
                  orElse(teams.findOneByID(key.replaceFirst("tx", "texas"))).
                  orElse(teams.findOneByID(key.replaceFirst("texas", "tx"))).
                  orElse(teams.findOneByID(key.replaceFirst("brooklyn", "ny"))).
                  orElse(teams.findOneByID(key.replaceFirst("ny", "brooklyn"))).
                  orElse {
      val name: String = gameTeam.name
      tryName(teams, name).
      orElse(tryName(teams, name.replaceFirst( """^St.""", "Saint"))).
      orElse(tryName(teams, name.replaceFirst( """St.$""", "State"))).
      orElse(tryName(teams, name.replaceFirst( """^Saint""", "St."))).
      orElse(tryName(teams, name.replaceFirst( """State$""", "St.")))
    }.orElse {
                    twitterTeam match {
                      case Some(tw: String) =>
                        teams.findOne(Map("twitterHandle" -> ("@" + tw)))
                      case None => None
                    }
                  }.orElse {
                    twitterSchool match {
                      case Some(tw: String) =>
                        teams.findOne(Map("twitterHandle" -> ("@" + tw)))
                      case None => None
                    }
                  }.orElse {
                    facebook match {
                      case Some(fb: String) =>
                        teams.findOne(Map("facebookPage" -> fb)).orElse(teams.findOne(Map("twitterHandle" -> ("@" + fb))))
                      case None => None
                    }
                  }
    if (optTeam.isEmpty) Logger.info("Failed to load team " + key)
    optTeam
  }

  def tryName(teams: casbah.MongoCollection, name: String): Option[teams.type#T] = {
    teams.findOne(Map("name" -> name))
  }
}

//TODO "status": {},
//TODO "alerts": {}
//TODO "champInfo": {},
