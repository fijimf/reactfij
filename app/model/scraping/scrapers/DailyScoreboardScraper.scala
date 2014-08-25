package model.scraping.scrapers

import java.io.PrintStream

import model.scraping.model.{Scoreboards, Scoreboard}
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

case object DailyScoreboardScraper {

  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext


  implicit def scoreboardReads: Reads[Scoreboard] = (
                                                      (JsPath \ "day").read[LocalDate](jodaLocalDateReads("EEEE, MMMM dd, yyyy")) and
                                                        (JsPath \ "games").read[Seq[String]]
                                                      )(Scoreboard.apply _)

  implicit def scoreboardsReads: Reads[Scoreboards] = (JsPath \ "scoreboard").read[Seq[Scoreboard]].map(Scoreboards)

  def loadDate[T](date: LocalDate, f: Scoreboards => T = dumpGames): Future[Either[Seq[(JsPath, Seq[ValidationError])], T]] = {
    loadDate(date.getYear, date.getMonthOfYear, date.getDayOfMonth, f)
  }

  def loadDate[T](year: Int, month: Int, day: Int, f: Scoreboards => T): Future[Either[Seq[(JsPath, Seq[ValidationError])], T]] = {
    val url: String = scoreboardUrl(year, month, day)
    WS.url(url).get().map(unwrapJson).map(s => {
      Json.fromJson[Scoreboards](Json.parse(s)) match {
        case JsSuccess(scoreboards, _) => Right(f(scoreboards))
        case JsError(errors) => Left(errors)
      }
    })
  }

  val dumpGames:Scoreboards=>Unit = showGames(_,System.out)


  def showGames(s: Scoreboards, out:PrintStream): Unit = {
    s.scoreboard.foreach(s => {
      out.println(s.day)
      s.games.foreach(g => {
        out.println(g)
      })
    })
  }

  def unwrapJson(ws: WSResponse): String = {
    ws.body.replaceFirst("^\\s*callbackWrapper\\(", "").replaceFirst("\\);$", "")
  }

  def scoreboardUrl(year: Int, month: Int, day: Int): String = {
    f"http://data.ncaa.com/jsonp/scoreboard/basketball-men/d1/$year%04d/$month%02d/$day%02d/scoreboard.json"
  }

  def main(args: Array[String]) {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy")
    print(new LocalDate().toString(fmt))
    val localDate: LocalDate = LocalDate.parse("Wednesday, February 12, 2014", fmt)
    test()
  }

  def test() {
    val testInput: String = """
    {
      "scoreboard": [
        {
          "day": "Wednesday, February 12, 2014",
          "games": [
            "/sites/default/files/data/game/basketball-men/d1/2014/02/12/winthrop-longwood/gameinfo.json",
            "/sites/default/files/data/game/basketball-men/d1/2014/02/12/loyola-maryland-lafayette/gameinfo.json",
            "/sites/default/files/data/game/basketball-men/d1/2014/02/12/navy-bucknell/gameinfo.json"
          ]
        }
      ]
    }
                            """
    val result: JsResult[Scoreboards] = Json.fromJson[Scoreboards](Json.parse(testInput))
    print(result.toString)
  }


}


