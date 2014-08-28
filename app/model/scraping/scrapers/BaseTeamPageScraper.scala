package model.scraping.scrapers
import org.jsoup._
import org.jsoup.nodes._
import org.jsoup.select._
import play.api.libs.ws.WS
import scala.concurrent.Future

case object BaseTeamPageScraper {
  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.collection.JavaConverters._

  def loadPage[T](key: String): Future[String] = {
    WS.url(f"http://www.ncaa.com/schools/$key%s").get().map(s => {
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

    def extractSocial(d: Document): Iterator[(String, String)] = {
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

}
