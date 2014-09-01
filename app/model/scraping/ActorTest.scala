package model.scraping

import akka.actor.Status.Success
import model.scraping.data.TeamLink
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import scrapers._
import actors.{DailyScoreboardActor, GameInfoActor}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.joda.time.LocalDate

import scala.concurrent.Future
import scala.util.{Try, Failure}

object ActorTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val teamList: Future[Either[Seq[(JsPath, Seq[ValidationError])], Map[String, Map[String, Any]]]] = TeamListScraper.loadTeamList((seq: Seq[TeamLink]) => {
      seq.foldLeft(Map.empty[String, Map[String, Any]])((data: Map[String, Map[String, Any]], link: TeamLink) => {
        link match {
          case TeamLink(Some(name), url) => data + (url -> Map("baseName" -> name, "key" -> url))
          case _ => data
        }
      })
    })
    val z: Future[Map[String, Map[String, Any]]] = teamList.map((x: Either[Seq[(JsPath, Seq[ValidationError])], Map[String, Map[String, Any]]]) => {
      x match {
        case Left(tup) => Map.empty[String, Map[String, Any]]
        case Right(data) => data
      }
    })
    val zz: Future[Map[String, Map[String, Any]]] = z.flatMap(mapBasketballPage)
    zz

    val value: Any = zz.foreach(println(_))

    //    TeamPageScraper.loadPage("villanova").onComplete((triedMap: Try[Map[String, Any]]) => {
    //      triedMap.foreach((map: Map[String, Any]) => println(map))
    //    })
    //TeamPageScraper.loadPage("villanova")
    //
    //    val system: ActorSystem = ActorSystem("deepfij")
    //    val sa: ActorRef = system.actorOf(Props[DailyScoreboardActor], "scoreboard")
    //    val ga: ActorRef = system.actorOf(Props[GameInfoActor], "gameinfo")
    //    sa ! new LocalDate(2014, 2, 12)

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