package model.scraping

import java.util.UUID
import java.util.concurrent.TimeUnit
import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import model.scraping.actors.{GameStub, PlayerStub, TeamBuilder}
import model.scraping.data.TeamLink
import model.scraping.scrapers._
import org.joda.time.LocalDate
import play.api.Logger
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object TeamLoader {

  import scala.concurrent.ExecutionContext.Implicits.global

  def load(): Future[Iterable[(String, TeamBuilder)]] = {
    val updateId: String = UUID.randomUUID().toString;
    Logger.info("Uppdate ID is "+updateId)
    val list: Future[(Seq[TeamLink], LocalDate)] = TeamListScraper.loadTeamList()
    val teamsTimestamp: Future[LocalDate] = list.map(_._2)
    val teamList: Future[Map[String, TeamBuilder]] = list.map(_._1).map((teamLinks: Seq[TeamLink]) => {
      teamLinks.foldLeft(Map.empty[String, TeamBuilder])((data: Map[String, TeamBuilder], link: TeamLink) => {
        link match {
          case TeamLink(Some(name), key) => data + (key -> TeamBuilder(key, name))
          case TeamLink(None, key) => data
        }
      })
    })

    val enricher: Throttler[(String, TeamBuilder), (String, TeamBuilder)] = new Throttler[(String, TeamBuilder), (String, TeamBuilder)]((tup: (String, TeamBuilder)) => enrichTeam(tup._1, tup._2, updateId))

    teamList.flatMap((tl: Map[String, TeamBuilder]) => {
      val futures: Iterable[Future[(String, TeamBuilder)]] = tl.map((tuple: (String, TeamBuilder)) => {
        enricher(tuple)
      })
      Future.sequence(futures)
    })
  }

  def enrichTeam(key: String, tb: TeamBuilder, updateId:String): Future[(String, TeamBuilder)] = {
    val bbPageData = BasketballTeamPageScraper.loadPage(key, updateId)
    val pageData = TeamPageScraper.loadPage(key, updateId)

    for (pdData <- pageData;
         bbPdData <- bbPageData) yield {
      val ps: Option[List[PlayerStub]] = bbPdData.get("players").map(_.asInstanceOf[List[Map[String, String]]]).map(_.flatMap(data => PlayerStub.fromMap(data)))
      val gs: Option[List[GameStub]] = bbPdData.get("games").map(_.asInstanceOf[List[Map[String, String]]]).map(_.flatMap(data => GameStub.fromMap(data)))

      val tbc: TeamBuilder = tb.copy(
                                       division = bbPdData.get("Division:").map(_.asInstanceOf[String]),
                                       conference = bbPdData.get("Conf:").map(_.asInstanceOf[String]),
                                       colorNames = pdData.get("Colors:").map(_.asInstanceOf[String]),
                                       nickname = pdData.get("Nickname:").map(_.asInstanceOf[String]),
                                       location = pdData.get("Location:").map(_.asInstanceOf[String]),
                                       logoUrl = bbPdData.get("logoUrl").map(_.asInstanceOf[String]),
                                       officialName = bbPdData.get("officialName").map(_.asInstanceOf[String]),
                                       officialUrl = pdData.get("officialUrl").map(_.asInstanceOf[String]),
                                       twitterUrl = pdData.get("twitterUrl").map(_.asInstanceOf[String]),
                                       twitterHandle = pdData.get("twitterId").map(_.asInstanceOf[String]),
                                       facebookUrl = pdData.get("facebookUrl").map(_.asInstanceOf[String]),
                                       facebookPage = pdData.get("facebookPage").map(_.asInstanceOf[String]),
                                       playerStubs = ps.getOrElse(tb.playerStubs),
                                       gameStubs = gs.getOrElse(tb.gameStubs)
                                     )
      val tuple: (String, TeamBuilder) = key -> tbc
      tuple
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
