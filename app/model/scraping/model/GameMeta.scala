package model.scraping.model

case class GameMeta(

                     title: String,
                     description: String,
                     sport: String,
                     division: String,
                     gametype: String,
                     status: String,
                     period: String,
                     minutes: String,
                     seconds: String,
                     teams: Seq[TeamMeta]
                     )
