package model.scraping.model


case class GameTeam(
                     teamRank: String,
                     iconURL: String,
                     name: String,
                     nameRaw: String,
                     nameSeo: String,
                     shortname: String,
                     color: String,
                     social: TeamSocial,
                     description: String,
                     currentScore: String,
                     scoreBreakdown: Seq[String],
                     winner: String
                     )