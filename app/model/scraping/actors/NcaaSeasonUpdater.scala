package model.scraping.actors

import akka.actor.{Props, Actor}
import model.scraping.data.TeamLink
import model.scraping.scrapers.TeamListScraper
import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

import scala.concurrent.Future

case class TeamMap(fieldKeys:Set[String], data:Map[String,Map[String,Any]]) {
  def update(data:Map[String, Object]): Unit = {

  }
}


case class UpdateSeason(start:LocalDate, end:LocalDate, teamData:Map[String,Map[String,Any]])
class NcaaSeasonUpdater extends Actor {
 // val sbd = context.actorOf[Props[DailyScoreboardActor]]
  def receive:Receive = {
    case UpdateSeason(start, end, teamData) =>
//      val kernelFut: Future[Either[Seq[(JsPath, Seq[ValidationError])], Map[String, Map[String, Object]]]] = TeamListScraper.loadTeamList((links: Seq[TeamLink]) => {
//        links.foldLeft(Map.empty[String, Map[String, Object]])((d: Map[String, Map[String, Object]], link: TeamLink) => {
//          link.name match {
//            case Some(n) => d + (link.url -> Map("key" -> link.url, "name" -> n))
//            case None => d
//          }
//        })
//      })
//      kernelFut.map((e: Either[Seq[(JsPath, Seq[ValidationError])], Map[String, Map[String, Object]]]) =>
//        e match {
//          case Left(_) =>
//          case Right(teamKernel) => {
//            Iterator.iterate(start)(_.plusDays(1)).takeWhile(_.isBefore(end)).foreach(date=>{
//
//            })
//          }
//        }
//                   )

  }


  
  // vv Move to another Object vv
  // private def loadTeams = {
  //   TeamListLoader.loadTeamList((s:Seq[TeamLink]])=>{
  //     s.filter(_.name.isDefined).map(t=>(t.url->Map("name"->t.name, "key"->t.url))).toMap
  //   }).
}
