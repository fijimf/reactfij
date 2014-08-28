package model.scraping.data

case class TeamData(
                     teamId: Int,
                     playerHeader: PlayerHeader,
                     playerStats: Seq[PlayerLine],
                     playerTotals: PlayerTotals
                     )
