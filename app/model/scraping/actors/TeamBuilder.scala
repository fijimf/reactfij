package model.scraping.actors

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
  )

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
