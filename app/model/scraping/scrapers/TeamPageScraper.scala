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

  def loadPage[T](key: String): Future[Map[String, String]] = {
    WS.url(f"http://www.ncaa.com/schools/$key%s").get().map(s => {
      val d: Document = Jsoup.parse(s.body)
      extractMeta(d).toMap ++ extractSocial(d)
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

  def extractSocial(d: Document): Map[String, String] = {
    extractSocialItem(d, "school-social-website", "officialUrl", "officialWebsite") ++
      extractSocialItem(d, "school-social-twitter", "twitterUrl", "twitterId") ++
      extractSocialItem(d, "school-social-facebook", "facebookUrl", "facebookPage")
  }

  def extractSocialItem(d: Document, liClass: String, urlKey: String, textKey: String): Map[String, String] = {
    val website: Elements = d.select("li." + liClass)
    if (website.isEmpty) {
      Map.empty[String, String]
    } else {
      val websiteLink: Elements = website.get(0).select("a")
      if (websiteLink.isEmpty)
        Map.empty[String, String]
      else
        Map(urlKey -> websiteLink.get(0).attr("href"), textKey -> websiteLink.get(0).ownText())
    }
  }
}
