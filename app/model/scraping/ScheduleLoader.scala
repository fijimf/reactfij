package model.scraping

import java.util.concurrent.TimeUnit
import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import model.scraping.actors.{GameStub, PlayerStub, TeamBuilder}
import model.scraping.data.{GameInfo, Scoreboard, Scoreboards, TeamLink}
import model.scraping.scrapers._
import org.joda.time.{Days, ReadablePeriod, LocalDate}
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Await, Future}

object ScheduleLoader {

  import scala.concurrent.ExecutionContext.Implicits.global

  def gameInfo(url:String): Future[Option[GameInfo]] = {

    GameInfoScraper.loadGameUrl[GameInfo](url.replace( """/sites/default/files/data""", """http://data.ncaa.com/jsonp"""), (info: GameInfo) => info).map {
      case Left(_) => None
      case Right(x) => Some(x)
    }
  }

  def load(start:LocalDate, end:LocalDate): Future[Iterable[GameInfo]] = {
    val dates: List[LocalDate] = Iterator.iterate(start)(_.plus(Days.ONE)).takeWhile(!_.isAfter(end)).toList
    val lfos: List[Future[Option[Scoreboard]]] = dates.map((date: LocalDate) => {
      DailyScoreboardScraper.loadDate(date, _.scoreboard.headOption).map {
        case Left(_) => None
        case Right(x) => x
      }
    })
    Future.sequence(lfos.map(fos => fos.flatMap((os: Option[Scoreboard]) => {
      os match {
        case Some(sb: Scoreboard) => Future.sequence(sb.games.map(gameInfo(_))).map(_.flatten)
        case None => Future(Seq.empty[GameInfo])
      }
    }))).map(_.flatten)
  }

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))

    loadSchedule("2013-14", new LocalDate(2013,12,1), new LocalDate(2014,1,1) )
  }

  def loadSchedule(seasonKey:String, from:LocalDate, to:LocalDate) {
    val eventualScoreboards: Future[Iterable[GameInfo]] = load(from, to)
    val result: Iterable[GameInfo] = Await.result(eventualScoreboards, Duration(5,TimeUnit.MINUTES))

    result.foreach(gi=>println(gi))
  }
}


