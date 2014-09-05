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

import scala.concurrent.Future
import scala.util.{Try, Failure}

object ActorTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    TeamListScraper.loadTeamList().map((teamLinks: Seq[TeamLink]) => {
      teamLinks.foldLeft(Map.empty[String, TeamBuilder])((data: Map[String, TeamBuilder], link: TeamLink) => {
        link match {
          case TeamLink(Some(name), key) => data + (key -> TeamBuilder(key, name))
          case TeamLink(None, key) => data
        }
      })
    }).map((data: Map[String, TeamBuilder]) => {
      data.foldLeft(Map.empty[String, TeamBuilder])((enrichedData: Map[String, TeamBuilder], tup: (String, TeamBuilder)) => {
        val k = tup._1
        val d = tup._2
        val bbPageData = BasketballTeamPageScraper.loadPage(k)
        val pageData = TeamPageScraper.loadPage(k)

        val zzzzz: Future[Map[String, TeamBuilder]] = (for (pdData <- pageData;
                                                          bbPdData <- bbPageData) yield {
          k -> d.copy(
                       division = pdData.get("Division:"),
                       conference = pdData.get("Conference:"),
                       officialUrl = pdData.get("officialUrl"),
                       twitterUrl = pdData.get("twitterUrl"),
                       twitterHandle = pdData.get("twitterId"),
                       facebookUrl = pdData.get("facebookUrl"),
                       facebookPage = pdData.get("facebookPage")
                     )
        }).map(f => enrichedData + f)
        zzzzz
      })
    })
  }

 

  def mapBasketballPage(data: Map[String, Map[String, Any]]): Future[Map[String, Map[String, Any]]] = {
    val keys: Iterable[String] = data.keys
    keys.foldLeft(Future.successful(data))((results: Future[Map[String, Map[String, Any]]], teamKey: String) => {
      val fu: Future[Map[String, Any]] = BasketballTeamPageScraper.loadPage(teamKey)
      for (r <- results;
           f <- fu) yield {

        val tData = r(teamKey)

        r + (teamKey -> (tData ++ f))
      }
    })
  }

}