package model.scraping

import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.Status.Success
import akka.util.Timeout
import model.scraping.data.TeamLink
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import play.api.libs.ws.{WSResponse, WS}
import scrapers._
import model.scraping.actors.{TeamBuilder, TeamCoordinator, DailyScoreboardActor, GameInfoActor}
import akka.actor._
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
    Await.result(allData, Duration(10, TimeUnit.MINUTES))
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

object ThrottleTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val ref: ActorRef = ActorSystem("my-system").actorOf(Props[ThrottleActor])
    (1.to(1000)).foreach(n => {
      ref ! "http://www.amazon.com"
    })
  }


}

class ThrottleActor extends Actor {
  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global
  case object SubmitFromQueue

  private var outstandingRequests = 0
  private var pendingRequests = List.empty[(ActorRef, String)]

  override def receive: Actor.Receive = {
    case url: String => {
      Logger.info("Received external url request. Pending requests " + pendingRequests.size + ". Outstanding requests " + outstandingRequests)
      if (outstandingRequests < 10) {
        outstandingRequests = outstandingRequests + 1
        submitRequest(url, sender)
      } else {
        queueRequest(url, sender)
      }
    }
    case SubmitFromQueue => {
      Logger.info("Submitting queued request. Received external url request. Pending requests " + pendingRequests.size + ". Outstanding requests " + outstandingRequests)
      pendingRequests match {
        case (s, u) :: rest => {
          pendingRequests = rest
          outstandingRequests = outstandingRequests + 1
          submitRequest(u, s)
        }
        case Nil =>
      }
    }
    case _ =>
  }

  def submitRequest(url: String, sender: ActorRef) {
    Logger.info("Submitting request")
    WS.url(url).get().onComplete({
      case scala.util.Success(wsResponse) => {
        outstandingRequests = outstandingRequests - 1
        sender ! Future.successful(wsResponse)
        self ! SubmitFromQueue
      }
      case Failure(exception) => {
        outstandingRequests = outstandingRequests - 1
        sender ! Future.failed(exception)
        self ! SubmitFromQueue
      }
    })
  }

  def queueRequest(url: String, sender: ActorRef): Unit = {
    Logger.info("Queueing request.")
    pendingRequests::=(sender, url)
  }
}