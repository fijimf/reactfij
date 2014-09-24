package model.scraping

import java.util.concurrent.TimeUnit
import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import model.scraping.actors.{GameStub, PlayerStub, TeamBuilder}
import model.scraping.data.TeamLink
import model.scraping.scrapers._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ScheduleLoader {

  import scala.concurrent.ExecutionContext.Implicits.global

  def load(): Future[Iterable[(String, TeamBuilder)]] = {
    val teamList: Future[Map[String, TeamBuilder]] = TeamListScraper.loadTeamList().map((teamLinks: Seq[TeamLink]) => {
      teamLinks.foldLeft(Map.empty[String, TeamBuilder])((data: Map[String, TeamBuilder], link: TeamLink) => {
        link match {
          case TeamLink(Some(name), key) => data + (key -> TeamBuilder(key, name))
          case TeamLink(None, key) => data
        }
      })
    })

    val enricher: Throttler[(String, TeamBuilder), (String, TeamBuilder)] = new Throttler[(String, TeamBuilder), (String, TeamBuilder)]((tup: (String, TeamBuilder)) => enrichTeam(tup._1, tup._2))

    teamList.flatMap((tl: Map[String, TeamBuilder]) => {
      val futures: Iterable[Future[(String, TeamBuilder)]] = tl.map((tuple: (String, TeamBuilder)) => {
        enricher(tuple)
      })
      Future.sequence(futures)
    })
  }

  def enrichTeam(key: String, tb: TeamBuilder): Future[(String, TeamBuilder)] = {
    val bbPageData = BasketballTeamPageScraper.loadPage(key)
    val pageData = TeamPageScraper.loadPage(key)

    for (pdData <- pageData;
         bbPdData <- bbPageData) yield {
      val ps: Option[List[PlayerStub]] = bbPdData.get("players").map(_.asInstanceOf[List[Map[String, String]]]).map(_.flatMap(data => PlayerStub.fromMap(data)))
      val gs: Option[List[GameStub]] = bbPdData.get("games").map(_.asInstanceOf[List[Map[String, String]]]).map(_.flatMap(data => GameStub.fromMap(data)))
      key -> tb.copy(
                      division = bbPdData.get("Division:").map(_.asInstanceOf[String]),
                      conference = bbPdData.get("Conf:").map(_.asInstanceOf[String]),
                      colorNames = pdData.get("Colors:"),
                      nickname = pdData.get("Nickname:"),
                      location = pdData.get("Location:"),
                      officialUrl = pdData.get("officialUrl"),
                      twitterUrl = pdData.get("twitterUrl"),
                      twitterHandle = pdData.get("twitterId"),
                      facebookUrl = pdData.get("facebookUrl"),
                      facebookPage = pdData.get("facebookPage"),
                      playerStubs = ps.getOrElse(tb.playerStubs),
                      gameStubs = gs.getOrElse(tb.gameStubs)
                    )
    }
  }

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val client = MongoClient("localhost", 27017)

    loadTeamKernel(client,"2013-14")
  }

  def loadTeamKernel(client: casbah.MongoClient, seasonKey:String) {
    val result: Iterable[(String, TeamBuilder)] = Await.result(load(), Duration(15, TimeUnit.MINUTES))
    result.foreach((tuple: (String, TeamBuilder)) => {
      TeamBuilder.upsertTeam(client, tuple._2, seasonKey)
    })
  }
}
