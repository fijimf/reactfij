package model.scraping.actors

import akka.actor.{ActorRef, Props, Actor}
import akka.actor.Actor.Receive
import model.scraping.actors.TeamCoordinator.{Status, Results, Start}
import model.scraping.data.TeamLink
import model.scraping.scrapers.{BasketballTeamPageScraper, TeamListScraper}
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

import scala.concurrent.Future
import scala.util.Try

case class TeamBuilder
(
  key:String,
  name:String,
  division:Option[String]=None,
  conference:Option[String]=None,
  bbDivision:Option[String]=None,
  bbConference:Option[String]=None,
  officialUrl:Option[String]=None,
  facebookPage:Option[String]=None,
  facebookUrl:Option[String]=None,
  twitterHandle:Option[String]=None,
  twitterUrl:Option[String]=None,
  playerStubs:List[(String, String, String)]= List.empty[(String, String, String)],
  gameStubs:List[(String, String, String, String)]= List.empty[(String, String, String, String)]
  )

object TeamCoordinator {
  case object Start
  case object Status
  case object Results
}
class TeamCoordinator extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext


  private val btpa: ActorRef = context.actorOf(Props[BasketballTeamPageActor])
  private val tpa: ActorRef = context.actorOf(Props[TeamPageActor])

  var teamData:Option[Map[String, TeamBuilder]]=None
  var teamPageRequest:Option[Set[String]] = None
  var basketballTeamPageRequest:Option[Set[String]] = None

  override def receive: Receive = {
    case Start =>{
      Logger.info("Starting team coordinator")
      TeamListScraper.loadTeamList((seq: Seq[TeamLink]) => {
        seq.foldLeft(Map.empty[String, TeamBuilder])((results: Map[String, TeamBuilder], link: TeamLink) => {
          link.name match {
            case Some(t) => results + (link.url -> TeamBuilder(link.url, t))
            case _ => results
          }
        })
      }).onComplete((t: Try[Either[_, Map[String, TeamBuilder]]]) => {
        Logger.info("Received team list.  Submitteing requests")
        for (tt<-t) yield {
          tt match {
            case Left(_) =>
            case Right(map) => {
               teamData=Some(map)
               teamPageRequest=Some(map.keySet)
               basketballTeamPageRequest=Some(map.keySet)
               map.keys.foreach(k=>{
                  tpa ! TeamPageReq(k)
                  btpa ! BasketballTeamPageReq(k)
               })
            }
          }
        }
        Logger.info("Done submitting requests")
      })
    }
    case BasketballTeamPageResp(url, data)=>{
      Logger.info("Handing basketball page - "+url)
      teamData.map(td => {
        val map: Option[TeamBuilder] = td.get(url).map(team => {
          team.copy(bbDivision = data.get("Div:").map(_.toString))
          team.copy(bbConference = data.get("Conference:").map(_.toString))
        })

        map.foreach(tb => teamData = Some(td + (url -> tb)))
      })
    }
    case TeamPageResp(url, data)=>{
      Logger.info("Handing team page - "+url)
      teamData.map(td => {
        val map: Option[TeamBuilder] = td.get(url).map(team => {
          team.copy(division = data.get("Div:").map(_.toString))
          team.copy(conference = data.get("Conference:").map(_.toString))
        })
        map.foreach(tb => teamData = Some(td + (url -> tb)))
      })

    }
    case Status =>
      val pendingTeamReqs: Int = teamPageRequest.map(_.size).getOrElse(-1)
      val pendingBasketballTeamReqs: Int = basketballTeamPageRequest.map(_.size).getOrElse(-1)
      sender() ! (pendingTeamReqs, pendingBasketballTeamReqs)
    case Results =>
      sender() ! teamData.getOrElse(Map.empty[String,TeamBuilder])
  }
}
