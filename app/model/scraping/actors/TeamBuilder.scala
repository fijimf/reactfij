package model.scraping.actors

import java.io.Serializable

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import play.api.Logger

case class TeamBuilder
(
  key: String,
  name: String,
  division: Option[String] = None,
  conference: Option[String] = None,
  colorNames: Option[String] = None,
  nickname: Option[String] = None,
  location: Option[String] = None,
  color: Option[String] = None,
  logoUrl: Option[String] = None,
  officialName: Option[String] = None,
  officialUrl: Option[String] = None,
  facebookPage: Option[String] = None,
  facebookUrl: Option[String] = None,
  twitterHandle: Option[String] = None,
  twitterUrl: Option[String] = None,
  playerStubs: List[PlayerStub] = List.empty[PlayerStub],
  gameStubs: List[GameStub] = List.empty[GameStub]
  ) {
  def toMongoObj(seasonKey: String): Imports.MongoDBObject = {
    val seasonalData: Map[String, Serializable] = Map(
                                                       "key" -> seasonKey,
                                                       "players" -> playerStubs.map(p => Map("number" -> p.number, "name" -> p.name, "pos" -> p.pos, "height" -> p.height, "year" -> p.year)),
                                                       "games" -> gameStubs.map(g => Map("date" -> g.date, "homeAway" -> g.homeAway, "oppKey" -> g.oppKey))
                                                     ) ++ conference.map("conference" -> _)
    new MongoDBObject(
                       Map("key" -> key,
                            "name" -> name) ++
                         List("division" -> division,
                               "colorNames" -> colorNames,
                               "nickname" -> nickname,
                               "location" -> location,
                               "color" -> color,
                               "logoUrl" -> logoUrl,
                               "officialName" -> officialName,
                               "officialUrl" -> officialUrl,
                               "facebookPage" -> facebookPage,
                               "facebookUrl" -> facebookUrl,
                               "twitterHandle" -> twitterHandle,
                               "twitterUrl" -> twitterUrl).filter(_._2.isDefined).map(t => t._1 -> t._2.get).toMap ++
                         Map("season" -> seasonalData
                            ))
  }
}

object TeamBuilder {
  def upsertTeam(client: MongoClient, tb: TeamBuilder, seasonKey: String) = {
    val db = client("deepfij")
    val collection = db("teams")
    val q: Imports.MongoDBObject = new Imports.MongoDBObject(Map("_id" -> tb.key))
    val flag: Imports.MongoDBObject = new Imports.MongoDBObject(Map("multi" -> false, "upsert" -> true))
    val obj: Imports.MongoDBObject = tb.toMongoObj(seasonKey)
    Logger.info(obj.toString())
    collection.update(q, obj, upsert = true, multi = false)
    //Logger.info(result)
  }
}


case class PlayerStub(number: String, name: String, pos: String, height: String, year: String)

object PlayerStub {
  def fromMap(data: Map[String, String]): Option[PlayerStub] = {
    for (
      number <- data.get("number");
      name <- data.get("name");
      pos <- data.get("pos");
      height <- data.get("height");
      year <- data.get("class")
    ) yield {
      PlayerStub(number, name, pos, height, year)
    }
  }
}

case class GameStub(date: String, homeAway: String, oppKey: String)

object GameStub {
  def fromMap(data: Map[String, String]): Option[GameStub] = {
    for (
      date <- data.get("date");
      homeAway <- data.get("ha");
      oppKey <- data.get("oppKey")
    ) yield {
      GameStub(date, homeAway, oppKey)
    }
  }
}

case class ScrapingMetaData(updateId: String, url:String, timestamp:LocalDate)

object ScrapingMetaData {

}
