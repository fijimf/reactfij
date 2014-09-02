package model.scraping

import java.util.concurrent.TimeUnit

import akka.actor.Status.Success
import akka.util.Timeout
import model.scraping.data.TeamLink
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import scrapers._
import model.scraping.actors.{TeamCoordinator, DailyScoreboardActor, GameInfoActor}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.joda.time.LocalDate

import scala.concurrent.Future
import scala.util.{Try, Failure}

object ActorTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    implicit val timeout = Timeout(15, TimeUnit.SECONDS)
    val system: ActorSystem = ActorSystem("deepfij")
    val tc: ActorRef = system.actorOf(Props[TeamCoordinator], "team-coordinator")
    tc ! TeamCoordinator.Start

    while (true)
      akka.pattern.ask(tc, TeamCoordinator.Status).onComplete((value: Try[Any]) => {
        for (v <- value) {
          v match {
            case Success(tup) => println("*********** " + tup.toString)
            case _ =>
          }
        }
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