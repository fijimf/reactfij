package model.scraping.actors

import com.mongodb.casbah.{TypeImports, Imports}
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
  officialUrl: Option[String] = None,
  facebookPage: Option[String] = None,
  facebookUrl: Option[String] = None,
  twitterHandle: Option[String] = None,
  twitterUrl: Option[String] = None,
  playerStubs: List[PlayerStub] = List.empty[PlayerStub],
  gameStubs: List[GameStub] = List.empty[GameStub]
  ) {
  def toMongoObj: Imports.MongoDBObject = {
    new MongoDBObject(
                       Map("key" -> key,
                            "name" -> name) ++
                         List("division" -> division,
                               "conference" -> conference,
                               "colorNames" -> colorNames,
                               "nickname" -> nickname,
                               "location" -> location,
                               "color" -> color,
                               "logoUrl" -> logoUrl,
                               "officialUrl" -> officialUrl,
                               "facebookPage" -> facebookPage,
                               "facebookUrl" -> facebookUrl,
                               "twitterHandle" -> twitterHandle,
                               "twitterUrl" -> twitterUrl).filter(_._2.isDefined).map(t => t._1 -> t._2.get).toMap ++
                         Map("players" -> playerStubs.map(p => Map("number" -> p.number, "name" -> name, "pos" -> p.pos, "height" -> p.height, "year" -> p.year))) ++
                         Map("games" -> gameStubs.map(g => Map("date" -> g.date, "homeAway" -> g.homeAway, "oppKey" -> g.oppKey)))
                     )
  }
}

object TeamBuilder {
  def upsertTeam(client: MongoClient, tb: TeamBuilder) = {
    val db = client("deepfij")
    val collection = db("teams")
    val q: Imports.MongoDBObject = new Imports.MongoDBObject(Map("_id" -> tb.key))
    val flag: Imports.MongoDBObject = new Imports.MongoDBObject(Map("multi" -> false, "upsert" -> true))
    collection.update(q, tb.toMongoObj, upsert = true, multi = false)
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
