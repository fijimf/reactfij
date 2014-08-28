package model.scraping.data

import java.util.TimeZone

import org.joda.time.{DateTimeZone, DateTime, LocalTime, LocalDate}

case class GameInfo(
                     id: String,
                     conference: String,
                     gameState: String,
                     startDate: LocalDate,
                     startTime: LocalTime,
                     finalMessage: String,
                     gameStatus: String,
                     location: String,
                     contestName: String,
                     url: String,
                     scoreBreakdown: Seq[String],
                     home: GameTeam,
                     away: GameTeam,
                     tabsArray: Seq[Seq[GameLinks]]
                     ) {
  def gameId: Int = id.toInt

  def conferences: Seq[String] = conference.split(" ")

  def gameTime: DateTime = startDate.toDateTime(startTime, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New York")))

}


//TODO "status": {},
//TODO "alerts": {}
//TODO "champInfo": {},
