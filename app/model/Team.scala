package model

case class Team(
                 name: String,
                 nickname: String,
                 longName: Option[String],
                 shortName: Option[String],
                 officialUrl: Option[String],
                 logoUrl: Option[String],
                 primaryColor: Option[String],
                 secondaryColor: Option[String],
                 location: Option[Location]
                 )
