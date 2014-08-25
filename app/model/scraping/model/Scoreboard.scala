package model.scraping.model

import org.joda.time.LocalDate

case class Scoreboard(day: LocalDate, games: Seq[String])

