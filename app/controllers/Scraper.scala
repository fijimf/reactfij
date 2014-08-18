package controllers

import play.api._
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSResponse, WS}
import play.api.mvc._

import scala.concurrent.Future


object Scraper extends Controller {

  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def gameBox = Action.async {
    val resp: Future[String] = WS.url("http://data.ncaa.com/jsonp/game/basketball-men/d1/2014/02/16/lipscomb-kennesaw-st/boxscore.json").get().map(_.body.replaceFirst("^\\s*callbackWrapper\\(", "").replaceFirst("\\);$", ""))
     resp.map(s => Ok(Json.parse(s)))
  }


  def boxSvg = Action {

     Ok(views.html.index2("testbed"))
  }

  def scrapeTeam = {}

}