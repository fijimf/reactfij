package model.scraping

import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

object DailyScoreboardScraper {

  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  case class Scoreboard(day:LocalDate, games:Seq[String])
  case class Scoreboards(scoreboard:Seq[Scoreboard])

  implicit def scoreboardReads:Reads[Scoreboard] = (
    (JsPath \ "day").read[LocalDate](jodaLocalDateReads("EEEE, MMMM dd, yyyy")) and
      (JsPath \ "games").read[Seq[String]]
    )(Scoreboard.apply _)

  implicit def scoreboardsReads: Reads[Scoreboards] = (JsPath \ "scoreboard").read[Seq[Scoreboard]].map(Scoreboards)

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

  def loadDate(year: Int, month: Int, day: Int, f: Scoreboards => Unit = processScoreboards):Unit= {
    val url: String = scoreboardUrl(year, month, day)
    WS.url(url).get().map(unwrapJson).map(s => {
      Json.fromJson[Scoreboards](Json.parse(s)) match {
        case JsSuccess(scoreboards,_) => f(scoreboards)
        case _ => Logger.error(s"Error parsing date $year-$month-$day")
      }
    })
  }

  def processScoreboards(s:Scoreboards):Unit ={
    s.scoreboard.foreach(s=>{
        print(s.day)
        s.games.foreach(g=>{
          print(gameUrl(g))
          WS.url(gameUrl(g)).get().map(unwrapJson).map(s => {
            print(s)
          })
        })
    })
  }



  def unwrapJson(ws:WSResponse):String = {
    ws.body.replaceFirst("^\\s*callbackWrapper\\(", "").replaceFirst("\\);$", "")
  }

  def scoreboardUrl(year: Int, month: Int, day: Int): String = {
     f"http://data.ncaa.com/jsonp/scoreboard/basketball-men/d1/$year%04d/$month%02d/$day%02d/scoreboard.json"
  }

  def gameUrl(scoreboardGame:String):String = {
     scoreboardGame.replace("""/sites/default/files/data""",""""http://data.ncaa.com/jsonp""")
  }
}


