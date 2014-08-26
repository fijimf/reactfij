package model.scraping.actors

case class TeamMap(fieldKeys:Set[String], data:Map[String,Map[String,Any]]) {
  def update
}


case class UpdateSeason(start:Localdate, end:LocalDate, teamData:Map[String,Map[String,Any]])
class NcaaSeasonUpdater extends Actor {

  def receive {
    case UpdateSeason(start, end, teamData) => 
      
  
    case 
  }
  
  // vv Move to another Object vv
  // private def loadTeams = {
  //   TeamListLoader.loadTeamList((s:Seq[TeamLink]])=>{
  //     s.filter(_.name.isDefined).map(t=>(t.url->Map("name"->t.name, "key"->t.url))).toMap
  //   }).
}
