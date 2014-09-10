package model.scraping

import java.util.concurrent.TimeUnit

import akka.actor.Status.Success
import akka.util.Timeout
import model.scraping.data.TeamLink
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import scrapers._
import model.scraping.actors.{TeamBuilder, TeamCoordinator, DailyScoreboardActor, GameInfoActor}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.joda.time.LocalDate

import scala.collection.immutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Try, Failure}

object ActorTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val allData: Future[Map[String, TeamBuilder]] = TeamListScraper.loadTeamList().map((teamLinks: Seq[TeamLink]) => {
      teamLinks.foldLeft(Map.empty[String, TeamBuilder])((data: Map[String, TeamBuilder], link: TeamLink) => {
        link match {
          case TeamLink(Some(name), key) => data + (key -> TeamBuilder(key, name))
          case TeamLink(None, key) => data
        }
      })
    }).flatMap((data: Map[String, TeamBuilder]) => {
      val futures: Iterable[Future[(String, TeamBuilder)]] = data.map((tup: (String, TeamBuilder)) => {
        val team = tup._1
        val teamData = tup._2
        enrichTeam(team, teamData)
      })
      val sequence: Future[Iterable[(String, TeamBuilder)]] = Future.sequence(futures)
      sequence.map(_.toMap)
    })
    Await.result(allData, Duration(10,TimeUnit.MINUTES))
  }

  def enrichTeam(key: String, tb: TeamBuilder): Future[(String, TeamBuilder)] = {
    val bbPageData = BasketballTeamPageScraper.loadPage(key)
    val pageData = TeamPageScraper.loadPage(key)

    for (pdData <- pageData;
         bbPdData <- bbPageData) yield {
      key -> tb.copy(
                      division = pdData.get("Division:"),
                      conference = pdData.get("Conference:"),
                      officialUrl = pdData.get("officialUrl"),
                      twitterUrl = pdData.get("twitterUrl"),
                      twitterHandle = pdData.get("twitterId"),
                      facebookUrl = pdData.get("facebookUrl"),
                      facebookPage = pdData.get("facebookPage")
                    )
    }
  }
}