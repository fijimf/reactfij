package model.scraping.data

case class BoxScore(
                     meta: GameMeta,
                     teams: Seq[TeamData]
                     )
