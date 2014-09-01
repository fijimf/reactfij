package model.scraping.scrapers

import java.io.PrintStream

import model.scraping.data._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{LocalDate, LocalTime}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

case object GameInfoScraper {

  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def jodaLocalTimeReads(pattern: String, corrector: String => String = identity): Reads[org.joda.time.LocalTime] = new Reads[org.joda.time.LocalTime] {

    import org.joda.time.format.DateTimeFormat

    val df = DateTimeFormat.forPattern(pattern)

    def reads(json: JsValue): JsResult[LocalTime] = json match {
      case JsString(s) => parseDate(corrector(s)) match {
        case Some(d) => JsSuccess(d)
        case None => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.jodadate.format", pattern))))
      }
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.date"))))
    }

    private def parseDate(input: String): Option[LocalTime] =
      scala.util.control.Exception.allCatch[LocalTime] opt LocalTime.parse(input, df)
  }


  implicit def gameInfoReads: Reads[GameInfo] = (
                                                  (JsPath \ "id").read[String] and
                                                    (JsPath \ "conference").read[String] and
                                                    (JsPath \ "gameState").read[String] and
                                                    (JsPath \ "startDate").read[LocalDate] and
                                                    (JsPath \ "startTime").read[LocalTime](jodaLocalTimeReads("h:mm", (s: String) => s.split(" ")(0))) and
                                                    (JsPath \ "finalMessage").read[String] and
                                                    (JsPath \ "gameStatus").read[String] and
                                                    (JsPath \ "location").read[String] and
                                                    (JsPath \ "contestName").read[String] and
                                                    (JsPath \ "url").read[String] and
                                                    (JsPath \ "scoreBreakdown").read[Seq[String]] and
                                                    (JsPath \ "home").read[GameTeam] and
                                                    (JsPath \ "away").read[GameTeam] and
                                                    (JsPath \ "tabsArray").read[Seq[Seq[GameLinks]]]
                                                  )(GameInfo.apply _)

  implicit def gameTeamReads: Reads[GameTeam] = (
                                                  (JsPath \ "teamRank").read[String] and
                                                    (JsPath \ "iconURL").read[String] and
                                                    (JsPath \ "name").read[String] and
                                                    (JsPath \ "nameRaw").read[String] and
                                                    (JsPath \ "nameSeo").read[String] and
                                                    (JsPath \ "shortname").read[String] and
                                                    (JsPath \ "color").read[String] and
                                                    (JsPath \ "social").read[TeamSocial] and
                                                    (JsPath \ "description").read[String] and
                                                    (JsPath \ "currentScore").read[String] and
                                                    (JsPath \ "scoreBreakdown").read[Seq[String]] and
                                                    (JsPath \ "winner").read[String]
                                                  )(GameTeam.apply _)

  implicit def teamSocialReads: Reads[TeamSocial] = (
                                                      (JsPath \ "twitter").readNullable[SocialInstance] and
                                                        (JsPath \ "facebook").readNullable[SocialInstance]
                                                      )(TeamSocial.apply _)

  implicit def socialInstanceReads: Reads[SocialInstance] = ((JsPath \ "keywords").read[Seq[String]] and
    (JsPath \ "accounts").read[SocialAccountList]
                                                              )(SocialInstance.apply _)

  implicit def socialAccountListReads: Reads[SocialAccountList] = (
                                                                    (JsPath \ "ncaa").readNullable[String] and
                                                                      (JsPath \ "athleticDept").readNullable[String] and
                                                                      (JsPath \ "conference").readNullable[String] and
                                                                      (JsPath \ "sport").readNullable[String]
                                                                    )(SocialAccountList.apply _)

  implicit def gameLinksReads: Reads[GameLinks] = (
                                                    (JsPath \ "type").read[String] and
                                                      (JsPath \ "title").read[String] and
                                                      (JsPath \ "file").read[String]
                                                    )(GameLinks.apply _)


  def loadGameUrl[T](url: String, f: GameInfo => T = dumpGameInfo): Future[Either[Seq[(JsPath, Seq[ValidationError])], T]] = {
    WS.url(url).get().map(unwrapJson).map(s => {
      Json.fromJson[GameInfo](Json.parse(s)) match {
        case JsSuccess(scoreboards, _) => Right(f(scoreboards))
        case JsError(errors) => Left(errors)
      }
    })
  }

  val dumpGameInfo: GameInfo => Unit = showGameInfo(_, System.out)


  def showGameInfo(s: GameInfo, out: PrintStream): Unit = {
    out.println(s.id + " " + s.away.name + " @ " + s.home.name + " " + s.startDate)
  }

  def unwrapJson(ws: WSResponse): String = {
    ws.body.replaceFirst("^\\s*callbackWrapper\\(", "").replaceFirst("\\);$", "")
  }

  def main(args: Array[String]) {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("hh:mm")
    println(new LocalTime().toString(fmt))
    val localTime: LocalTime = LocalTime.parse("4:00", fmt)
    println(localTime)
    test()
  }

  def test() {
    val testInput: String = """
    {
      "id":"357057",
		  "conference":"all-conf big-south",
		  "gameState":"final",
		  "startDate":"2014-02-12",
			"startDateDisplay":"Feb. 12",
		  "startTime":"4:00  ET",
			"startTimeEpoch":"1392220800",
		  "currentPeriod":"Final",
		  "finalMessage":"Final",
		  "gameStatus":"Final",
		  "periodStatus":"In-Progress",
		  "downToGo":"",
		  "timeclock":"",		     
		  "network_logo":"",
		  "location":"Willett Hall, Farmville, Virginia",
		  "contestName":"",
		  "url":"/game/basketball-men/d1/2014/02/12/winthrop-longwood",
		  "highlightsUrl":"", 
		  "liveAudioUrl":"",
		  "gameCenterUrl":"",
		  "champInfo": {  },
		  "videos":[],
	    "scoreBreakdown":[
	   	  "1",
	   		"2"],
		  "home": {
		      "teamRank":"0",
		      "iconURL":"/sites/default/files/images/logos/schools/l/longwood.70.png",
		      "name":"<a href='/schools/longwood'>Longwood</a>",
          "nameRaw":"Longwood",
          "nameSeo":"longwood",
		      "shortname":"LONGWD",
		      "color":"#1a5595",
		      "social":{
		        "twitter":{
		          "keywords":[],
		          "accounts":{
		            "ncaa":"marchmadness",
		            "athleticDept":"LongwoodLancers",
		            "conference":"BigSouthSports"
		          }
		        }
		      },
		      "description":"7-19",
		      "currentScore":"59",
		      "scoreBreakdown":[
	   			  "25",
	   			  "34"
	   			],
		      "winner":"false"
		     },
		     "away": {
		       "teamRank":"0",
		         "iconURL":"/sites/default/files/images/logos/schools/w/winthrop.70.png",
		         "name":"<a href='/schools/winthrop'>Winthrop</a>",
             "nameRaw":"Winthrop",
             "nameSeo":"winthrop",
		         "shortname":"WINTHR",
		      	 "color":"#ffbe00",		         
		         "social":{
		           "twitter":{
		             "keywords":[],
		             "accounts":{
		               "ncaa":"marchmadness",
		               "athleticDept":"WUEagles",
		               "conference":"BigSouthSports"
		             }
		           }
		         },
		         "description":"14-10",
		         "currentScore":"76",
		         "scoreBreakdown":[
	   			     "43",
	   			     "33"
	   			   ],
		         "winner":"true"
		       },
			     "tabs": "/sites/default/files/data//game/basketball-men/d1/2014/02/12/winthrop-longwood/tabs.json",
	    		 "tabsArray":  [[

			{"type":"recap", "title":"Recap", "file":"/sites/default/files/data/game/basketball-men/d1/2014/02/12/winthrop-longwood/recap.json"},
			{"type":"boxscore", "title":"Box Score", "file":"/sites/default/files/data/game/basketball-men/d1/2014/02/12/winthrop-longwood/boxscore.json"}
		]],
	    		 "status": {},
    			 "alerts": {}
		}
                            """

    val result: JsResult[GameInfo] = Json.fromJson[GameInfo](Json.parse(testInput))
    print(result.toString)
  }
}
