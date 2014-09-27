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
import scala.concurrent.{Await, Future}

object ScheduleLoader {

  import scala.concurrent.ExecutionContext.Implicits.global

  def load(start:LocalDate, end:LocalDate): Future[Iterable[GameInfo]] = {
    val dates: List[LocalDate] = Iterator.iterate(start)(_.plus(Days.ONE)).takeWhile(!_.isAfter(end)).toList
    val map: List[Future[Option[Scoreboard]]] = dates.map((date: LocalDate) => {
      DailyScoreboardScraper.loadDate(date, _.scoreboard.headOption).map {
        case Left(_) => None
        case Right(x) => x
      }
    })
    map.map(_.flatMap(ms=>{
      case Some(sb)=>{
        sb.
      }
      case None=> future(None)
    }))
    scoreboards.map(_.map((sb: Scoreboard) =>{
      val data:Seq[Future[Option[GameInfo]]]=sb.games.map(gameInfoUrl => {
        GameInfoScraper.loadGameUrl[GameInfo](gameInfoUrl, (info: GameInfo) => info).map {
          case Left(_) => None
          case Right(x) => Some(x)
        }
      })
                   data                       }
                   ))
  }

  def main(args: Array[String]) {
    new play.core.StaticApplication(new java.io.File("."))

    loadSchedule("2013-14", new LocalDate(2013,12,1), new LocalDate(2014,1,1) )
  }

  def loadSchedule(seasonKey:String, from:LocalDate, to:LocalDate) {
    val eventualScoreboards: Future[Iterable[Scoreboard]] = load(from, to)
    val result: Iterable[Scoreboard] = Await.result(eventualScoreboards, Duration(5,TimeUnit.MINUTES))

    println(result)
  }
}
