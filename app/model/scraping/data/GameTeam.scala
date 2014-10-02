package model.scraping.data


case class GameTeam(
                     teamRank: String,
                     iconURL: String,
                     schoolLink: String,
                     name: String,
                     key: String,
                     code: String,
                     color: String,
                     social: TeamSocial,
                     description: String,
                     currentScore: String,
                     scoreBreakdown: Seq[String],
                     winner: String
                     )