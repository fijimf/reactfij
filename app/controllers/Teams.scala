package controllers

import com.mongodb
import com.mongodb.{DBCollection, casbah}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import play.api._
import play.api.mvc._
import play.twirl.api.TemplateMagic.javaCollectionToScala

object Teams extends Controller {
  val client: MongoClient = MongoClient("localhost", 27017)
  val db: casbah.MongoDB = client("deepfij")
  val teamCollection: DBCollection = db.getCollection("teams")

  def show = Action {
    Ok(views.html.teams() )
  }

  def list = Action {
    val objects: List[mongodb.DBObject] = teamCollection.find(Map("division" -> "Div I")).sort(Map("name"->1)).to[List]
    Ok(com.mongodb.util.JSON.serialize(objects)).as("application/json")
  }

 def showTeam(key: String) = Action {
    Ok(views.html.team(key) )
  }

  def get(key:String) = Action {
    val one = teamCollection.findOne(MongoDBObject("_id" -> key))
    val team: mongodb.DBObject = one
    Ok(com.mongodb.util.JSON.serialize(team)).as("application/json")
  }

}