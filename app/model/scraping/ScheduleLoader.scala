package model.scraping

import java.util.concurrent.TimeUnit
import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import model.scraping.actors.{GameStub, PlayerStub, TeamBuilder}
import model.scraping.data.{GameInfo, Scoreboard, Scoreboards, TeamLink}
import model.scraping.scrapers._
import org.joda.time.{Days, ReadablePeriod, LocalDate}
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Await, Future}

object ScheduleLoader {

  import scala.concurrent.ExecutionContext.Implicits.global

  def gameInfo(url:String): Future[Option[GameInfo]] = {
    val newUrl: String = url.replace( """/sites/default/files/data""", """http://data.ncaa.com/jsonp""")
    Logger.info("Loading "+newUrl)
    GameInfoScraper.loadGameUrl[GameInfo](newUrl, (info: GameInfo) => info).map {
      case Left(_) => None
      case Right(x) => Some(x)
    }
  }

  def load(start:LocalDate, end:LocalDate): Future[Iterable[GameInfo]] = {
    val dates: List[LocalDate] = Iterator.iterate(start)(_.plus(Days.ONE)).takeWhile(!_.isAfter(end)).toList
    val lfos: List[Future[Option[Scoreboard]]] = dates.map((date: LocalDate) => {
      DailyScoreboardScraper.loadDate(date, _.scoreboard.headOption).map {
        case Left(_) => None
        case Right(x) => {
          Logger.info("For " + date + " loaded " + x.get.games.size + " game urls")
          x
        }
      }
    })
    val info:Throttler[String, Option[GameInfo]] = new Throttler[String, Option[GameInfo]](gameInfo)
    Future.sequence(lfos.map(fos => fos.flatMap((os: Option[Scoreboard]) => {
      os match {
        case Some(sb: Scoreboard) =>
          Future.sequence(sb.games.map(info(_))).map(_.flatten)
        case None => Future(Seq.empty[GameInfo])
      }
    }))).map(_.flatten)
  }

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))
    val client = MongoClient("localhost", 27017)


    loadSchedule(client, "2014-15", new LocalDate(2014,11,20), new LocalDate(2014,12,1) )
  }

  def loadSchedule(client:casbah.MongoClient, seasonKey:String, from:LocalDate, to:LocalDate) {
    val eventualInfos: Future[Iterable[GameInfo]] = load(from, to)
    Await.result(eventualInfos, Duration(15, TimeUnit.MINUTES))
    for (
      infos<-eventualInfos;
      info<-infos
    ) {
      GameInfo.mergeGameInfo(client, info, seasonKey);
    }

  }
}


