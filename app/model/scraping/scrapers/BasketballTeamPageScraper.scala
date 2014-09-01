package model.scraping.scrapers

import org.jsoup._
import org.jsoup.nodes._
import org.jsoup.select._
import play.api.Logger
import play.api.libs.ws.WS

import scala.concurrent.Future


case object BasketballTeamPageScraper {


  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  import scala.collection.JavaConverters._

  def loadPage[T](key: String): Future[Map[String, Any]] = {
    Logger.info("Loading "+key)
    WS.url(f"http://www.ncaa.com/schools/$key%s/basketball-men").get().map(s => {
      val d: Document = Jsoup.parse(s.body)
      val meta: Iterator[(String, String)] = extractMeta(d)
      Logger.info(meta.toMap.toString())
      meta.toMap ++ extractTables(d)
    })
  }

  def extractMeta(d: Document): Iterator[(String, String)] = {
    d.select("li.school-info").iterator().asScala.map(element => {
      val labels: Elements = element.select("span.school-meta-label")
      if (!labels.isEmpty) {
        val key = labels.get(0).ownText().trim()
        val value = element.ownText()
        Some(key -> value)
      } else {
        None
      }
    }).flatten
  }

  def extractTables(d: Document): Map[String, Any] = {
    val tables: Elements = d.select("table.ncaa-schools-sport-table")
    tables.iterator().asScala.foldLeft(Map.empty[String, Any])((map: Map[String, Any], table: Element) => {
      val theads: Elements = table.select("thead")
      if (isPlayerHeader(theads)) {
        map +("players"-> handlePlayerTable(table))
      } else if (isScheduleHeader(theads)) {
        map +("games"-> handleGameTable(table))
      } else {
        map
      }
    })
  }

  def isPlayerHeader(theads: Elements): Boolean = {
    if (!theads.isEmpty) {
      val cols: Elements = theads.get(0).select("tr th")
      (cols.size() > 2) && cols.get(1).ownText().equalsIgnoreCase("name") && cols.get(2).ownText().equalsIgnoreCase("position")
    } else {
      false
    }
  }

  def isScheduleHeader(theads: Elements): Boolean = {
    if (!theads.isEmpty) {
      val cols: Elements = theads.get(0).select("tr th")
      (cols.size() > 2) && cols.get(0).ownText().equalsIgnoreCase("date") && cols.get(1).ownText().equalsIgnoreCase("opponent")
    } else {
      false
    }
  }

  def handlePlayerTable(table: Element): List[Map[String, String]] = {
    val rows = table.select("tbody tr")
    rows.iterator().asScala.foldLeft(List.empty[Map[String, String]])((lst: List[Map[String, String]], row: Element) => {
      val pm = createPlayerMap(row.select("td"))
      if (pm.isEmpty) {
        lst
      } else {
        pm :: lst
      }
    })
  }

  def createPlayerMap(cells: Elements): Map[String, String] = {
    if (cells.size() > 1) {
      Map(
           "number" -> cells.get(0).ownText(),
           "name" -> cells.get(1).ownText(),
           "pos" -> (if (cells.size() > 2) cells.get(2).ownText() else ""),
           "height" -> (if (cells.size() > 3) cells.get(3).ownText() else ""),
           "class" -> (if (cells.size() > 4) cells.get(4).ownText() else "")
         )
    } else {
      Map.empty[String, String]
    }
  }

  def handleGameTable(table: Element): List[Map[String, String]] = {
    val rows = table.select("tbody tr")
    rows.iterator().asScala.foldLeft(List.empty[Map[String, String]])((lst: List[Map[String, String]], row: Element) => {
      val gm = createGameMap(row.select("td"))
      if (gm.isEmpty) {
        lst
      } else {
        gm :: lst
      }
    })
  }

  def createGameMap(cells: Elements): Map[String, String] = {
    if (cells.size() > 1) {
     val oppLink = cells.get(1).select("a")
      Map("date" -> cells.get(0).ownText(),
           "ha" -> cells.get(1).ownText(),
           "oppKey" -> oppLink.get(0).attr("href").replace("/schools/", ""),
           "oppName" -> oppLink.get(0).ownText())
    } else {
      Map.empty[String, String]
    }

  }
}