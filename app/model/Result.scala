package model

import play.api.libs.json.JsValue

case class Result(home:TeamBox, away:TeamBox, attendance:Option[Int], officials:Option[List[String]])

object Result {
  def fromNcaaJson(json:JsValue) :Option[Result] = {
    val h: Option[TeamBox] = (json\\"home").headOption.flatMap(b=>TeamBox.fromNcaaJson(b))
    val a: Option[TeamBox] = (json\\"away").headOption.flatMap(b=>TeamBox.fromNcaaJson(b))
    (h,a) match {
      case (Some(home),Some(away)) => Some(Result(home, away, None, None))
      case _ => None
    }


  }
}

