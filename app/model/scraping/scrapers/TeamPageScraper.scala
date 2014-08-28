package model.scraping.scrapers

import org.jsoup._
import org.jsoup.nodes._
import org.jsoup.select._
import play.api.libs.ws.WS

import scala.concurrent.Future


case object TeamPageScraper {


  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.collection.JavaConverters._

  def loadPage[T](key: String): Future[String] = {
    WS.url(f"http://www.ncaa.com/schools/$key%s/basketball-men").get().map(s => {
      val d: Document = Jsoup.parse(s.body)
      println(extractMeta(d).toMap)
      println(extractTables(d))
      "Hey Yo!"
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

  def extractTables(d: Document): Unit = {
    val tables: Elements = d.select("table.ncaa-schools-sport-table")
    tables.iterator().asScala.foreach(table => {
      val theads: Elements = table.select("thead")
      if (isPlayerHeader(theads)) {
        handlePlayerTable(table)
      } else if (isScheduleHeader(theads)) {
        handleGameTable(table)
      } else {
        System.err.println("Unknown table")
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

  def handlePlayerTable(table: Element): Iterator[(String, String, String, String, String )] = {
    val rows = table.select("tbody tr")
    rows.iterator().asScala.map(row => {
      val cells = row.select("td")
      if (cells.size() > 1) {
        val number = cells.get(0).ownText()
        val name = cells.get(1).ownText()
        val pos = if (cells.size() > 2) cells.get(2).ownText() else ""
        val hgt = if (cells.size() > 3) cells.get(3).ownText() else ""
        val cls = if (cells.size() > 4) cells.get(4).ownText() else ""
        println(number + " " + name + " " + pos)
        Some((number, name, pos, hgt, cls))
      } else {
        None
      }
    }).flatten
  }

  def handleGameTable(table: Element): Iterator[(String, String, String, String)] = {
    val rows = table.select("tbody tr")
    rows.iterator().asScala.map(row => {
      val cells = row.select("td")
      if (cells.size() > 1) {
        val date = cells.get(0).ownText()
        val ha = cells.get(1).ownText()
        val oppLink = cells.get(1).select("a")
        val oppKey = oppLink.get(0).attr("href").replace("/schools/", "")
        val oppName = oppLink.get(0).ownText()

        Some((date, ha , oppKey, oppName))
      }                                   else {
        None
      }
    }).flatten
  }


}
