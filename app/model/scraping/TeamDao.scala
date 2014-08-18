package model.scraping

import com.mongodb.casbah
import com.mongodb.casbah.{commons, Imports}
import com.mongodb.casbah.Imports._
import model.Location


case class TeamDao(client: MongoClient) {
  val db = client("deepfij")
  val collection = db("teams")

  def insertTeam(name: String,
                 nickname: String,
                 longName: Option[String] = None,
                 shortName: Option[String] = None,
                 officialUrl: Option[String] = None,
                 logoUrl: Option[String] = None,
                 primaryColor: Option[String] = None,
                 secondaryColor: Option[String] = None,
                 location: Option[Location] = None) = {
    collection.insert(addField("longName", longName) _ andThen
      addField("shortName", shortName) andThen
      addField("officialUrl", officialUrl) andThen
      addField("logoUrl", logoUrl) andThen
      addField("primaryColor", primaryColor) andThen
      addField("secondaryColor", secondaryColor) apply new MongoDBObject() ++("_id" -> name, "nickname" -> nickname))
  }

  def addField[T](k: String, f: Option[T])(s: Imports.MongoDBObject): Imports.MongoDBObject = {
    f match {
      case Some(v) => s ++ (k -> v)
      case _ => s
    }
  }

  def count() = {
    collection.size
  }

  def show() = {
    collection.find().foreach(print(_))
  }
}


object Junk {
  def main(args: Array[String]) {
    val client = MongoClient("localhost", 27017)
    val dao: TeamDao = TeamDao(client)
    print(dao.count())
    dao.show()
    dao.insertTeam("georgetown", "Hoyas")
    dao.insertTeam("villanova", "Wildcats", longName=Some("Villanova University"), shortName=Some("Villanova"))
    print(dao.count())
    dao.show()
  }
}