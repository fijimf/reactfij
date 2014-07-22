package model

import play.api.libs.json.JsValue

case class TeamBox(finalScore:Int, scoreByPeriod:List[Int],teamRebounds:Option[Int], playerTechnicals:Map[Player, Int], benchTechnicals:Int)


object TeamBox {
  def fromNcaaJson(json:JsValue) :Option[TeamBox] = {

    None
  }
}