package model.scraping.model

case class TeamData(
                     teamId: Int,
                     playerHeader: PlayerHeader,
                     playerStats: Seq[PlayerLine],
                     playerTotals: PlayerTotals
                     )
