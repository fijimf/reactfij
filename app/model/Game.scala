package model

import org.joda.time.DateTime

case class Game(season:Season, time:DateTime, homeTeam:Team, awayTeam:Team, result:Option[Result])

