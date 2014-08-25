package model.scraping.model

case class BoxScore(
                     meta: GameMeta,
                     teams: Seq[TeamData]
                     )
