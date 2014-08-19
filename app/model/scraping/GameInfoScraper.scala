package model.scraping

import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}

object GameInfoScraper {
  
  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  case class GameInfo (
    id:Int,
    conference:String,
    gameState:String,
    startDate:LocalDate,
    startTime:LocalTime,
    finalMessage:String,
    gameStatus:String,
    location:String,
    contestName:String,
    url:String,
    scoreBreakdown:Seq[String]
    home: GameTeam,
    away: GameTeam,
    tabsArray:Seq[GameLink]
  ) 
	//TODO "status": {},
        //TODO "alerts": {}
        //TODO "champInfo": {},

  case class GameTeam(
    teamRank:Int,
    iconURL:String,
    name:String,
    nameRaw:String,
    nameSeo:String,
    shortname:String,
    color:String,
    social:TeamSocial,
    description:String,
    currentScore:Int,
    scoreBreakdown:Seq[Int],
    winner:Boolean
  )
  
  case class TeamSocial(twitter:Option[SocialInstance], facebook:Option[SocialInstance])
		      
  case class SocialInstance(keywords:Seq[String], accounts:SocialAccountList)
  
  case class SocialAccountList(ncaa:Option[String], athleticDept:Option[String], conference:Option[String])
  
  case class GameLinks(type:String, title:String, file:String)

  implicit def gameInfoReads:Reads[GameInfo] = (
  	(JsPath \ "id").read[Int] and
  	(JsPath \ "conference").read[String] and
        (JsPath \ "gameState").read[String] and
        (JsPath \ "startDate").read[LocalDate] and
        (JsPath \ "startTime").read[LocalTime] and
        (JsPath \ "finalMessage").read[String] and
        (JsPath \ "gameStatus").read[String] and
        (JsPath \ "location").read[String] and
        (JsPath \ "contestName").read[String] and
        (JsPath \ "url").read[String] and
        (JsPath \ "scoreBreakdown").read[Seq[String]] and
        (JsPath \ "home").read[GameTeam] and
        (JsPath \ "away").read[GameTeam] and
        (JsPath \ "tabsArray").read[Seq[GameLinks]] and
  )(GameInfo.apply _)
  
 implicit def gameTeamReads:Reads[GameTeam] = (
  	(JsPath \ "teamRank").read[Int] and
        (JsPath \ "iconURL").read[String] and
	(JsPath \ "name").read[String] and
        (JsPath \ "nameRaw").read[String] and
        (JsPath \ "nameSeo").read[String] and
        (JsPath \ "shortname").read[String] and
	(JsPath \ "color").read[String] and
	(JsPath \ "social").read[TeamSocial] and
        (JsPath \ "description").read[String] and
        (JsPath \ "currentScore").read[Int] and
        (JsPath \ "scoreBreakdown").read[Seq[Int] and
        (JsPath \ "winner").read[Boolean] 
  )(GameTeam.apply _)
 
  implicit def teamSocialReads:Reads[TeamSocial] = (
  	(JsPath \ "twitter").readNullable[SocialInstance] and
        (JsPath \ "facebook").readNullable[SocialInstance]
  )(TeamSocial.apply _)
 		
  implicit def socialInstanceReads: Reads[SocialInstance] = (
  	(JsPath \ "keywords").read[Seq[String]] and
        (JsPath \ "accounts").read[SocialAccountList] 
  )(SocialInstance.apply _)
  
  implicit def socialAccountListReads: Reads[SocialAccountList] = (
  	(JsPath \ "ncaa".readNullable[String] and
        (JsPath \ "athleticDept".readNullable[String] and
        (JsPath \ "conference".readNullable[String] and
        (JsPath \ "sport".readNullable[String] //TODO
  )(SocialInstance.apply _)
  
  implicit def gameLinksReads: Reads[Gamelinks] = (
  	(JsPath \ "type").read[String] and
  	(JsPath \ "title").read[String] and
  	(JsPath \ "file").read[String]
  )(GameLink.apply _)

  def main(args: Array[String]) {
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
		});
    """
    
    val result: JsResult[Scoreboards] = Json.fromJson[Scoreboards](Json.parse(testInput))
    print(result.toString)
  }
}
