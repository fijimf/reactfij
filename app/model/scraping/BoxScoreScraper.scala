package model.scraping

import java.io.PrintStream

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{LocalDate, LocalTime}
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WSResponse, WS}

import scala.concurrent.Future

case object BoxScoreScraper {

  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext


  case class BoxScore(
                       meta: GameMeta,
                       teams: Seq[TeamData]
                       )

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

  case class TeamMeta(
                       homeTeam: String,
                       id: String,
                       seoName: String,
                       sixCharAbbr: String,
                       shortName: String,
                       nickName: String,
                       color: String
                       )

  case class TeamData(
                       teamId: Int,
                       playerHeader: PlayerHeader,
                       playerStats: Seq[PlayerLine],
                       playerTotals: PlayerTotals
                       )

  case class PlayerLine(
                         firstName: String,
                         lastName: String,
                         position: String,
                         minutesPlayed: String,
                         fieldGoalsMade: String,
                         threePointsMade: String,
                         freeThrowsMade: String,
                         totalRebounds: String,
                         offensiveRebounds: String,
                         assists: String,
                         personalFouls: String,
                         steals: String,
                         turnovers: String,
                         blockedShots: String,
                         points: String
                         )

  case class PlayerHeader(
                           position: String,
                           minutesPlayed: String,
                           fieldGoalsMade: String,
                           threePointsMade: String,
                           freeThrowsMade: String,
                           totalRebounds: String,
                           offensiveRebounds: String,
                           assists: String,
                           personalFouls: String,
                           steals: String,
                           turnovers: String,
                           blockedShots: String,
                           points: String
                           )

  case class PlayerTotals(
                           fieldGoalsMade: String,
                           threePointsMade: String,
                           freeThrowsMade: String,
                           totalRebounds: String,
                           offensiveRebounds: String,
                           assists: String,
                           personalFouls: String,
                           steals: String,
                           turnovers: String,
                           blockedShots: String,
                           points: String
                           )

  implicit def boxScoreReads: Reads[BoxScore] = (
                                                  (JsPath \ "meta").read[GameMeta] and
                                                    (JsPath \ "teams").read[Seq[TeamData]]
                                                  )(BoxScore.apply _)

  implicit def gameMetaReads: Reads[GameMeta] = (
                                                  (JsPath \ "title").read[String] and
                                                    (JsPath \ "description").read[String] and
                                                    (JsPath \ "sport").read[String] and
                                                    (JsPath \ "division").read[String] and
                                                    (JsPath \ "gametype").read[String] and
                                                    (JsPath \ "status").read[String] and
                                                    (JsPath \ "period").read[String] and
                                                    (JsPath \ "minutes").read[String] and
                                                    (JsPath \ "seconds").read[String] and
                                                    (JsPath \ "teams").read[Seq[TeamMeta]])(GameMeta.apply _)

  implicit def teamMetaReads: Reads[TeamMeta] = (
                                                  (JsPath \ "homeTeam").read[String] and
                                                    (JsPath \ "id").read[String] and
                                                    (JsPath \ "seoName").read[String] and
                                                    (JsPath \ "sixCharAbbr").read[String] and
                                                    (JsPath \ "shortName").read[String] and
                                                    (JsPath \ "nickName").read[String] and
                                                    (JsPath \ "color").read[String]
                                                  )(TeamMeta.apply _)

  implicit def teamDataReads: Reads[TeamData] = (
                                                  (JsPath \ "teamId").read[Int] and
                                                    (JsPath \ "playerHeader").read[PlayerHeader] and
                                                    (JsPath \ "playerStats").read[Seq[PlayerLine]] and
                                                    (JsPath \ "playerTotals").read[PlayerTotals]
                                                  )(TeamData.apply _)

  implicit def playerHeaderReads: Reads[PlayerHeader] = (
                                                          (JsPath \ "position").read[String] and
                                                            (JsPath \ "minutesPlayed").read[String] and
                                                            (JsPath \ "fieldGoalsMade").read[String] and
                                                            (JsPath \ "threePointsMade").read[String] and
                                                            (JsPath \ "freeThrowsMade").read[String] and
                                                            (JsPath \ "totalRebounds").read[String] and
                                                            (JsPath \ "offensiveRebounds").read[String] and
                                                            (JsPath \ "assists").read[String] and
                                                            (JsPath \ "personalFouls").read[String] and
                                                            (JsPath \ "steals").read[String] and
                                                            (JsPath \ "turnovers").read[String] and
                                                            (JsPath \ "blockedShots").read[String] and
                                                            (JsPath \ "points").read[String]
                                                          )(PlayerHeader.apply _)

  implicit def playerDataReads: Reads[PlayerLine] = (
                                                      (JsPath \ "firstName").read[String] and
                                                        (JsPath \ "lastName").read[String] and
                                                        (JsPath \ "position").read[String] and
                                                        (JsPath \ "minutesPlayed").read[String] and
                                                        (JsPath \ "fieldGoalsMade").read[String] and
                                                        (JsPath \ "threePointsMade").read[String] and
                                                        (JsPath \ "freeThrowsMade").read[String] and
                                                        (JsPath \ "totalRebounds").read[String] and
                                                        (JsPath \ "offensiveRebounds").read[String] and
                                                        (JsPath \ "assists").read[String] and
                                                        (JsPath \ "personalFouls").read[String] and
                                                        (JsPath \ "steals").read[String] and
                                                        (JsPath \ "turnovers").read[String] and
                                                        (JsPath \ "blockedShots").read[String] and
                                                        (JsPath \ "points").read[String]
                                                      )(PlayerLine.apply _)

  implicit def playerTotalsReads: Reads[PlayerTotals] = (
                                                          (JsPath \ "fieldGoalsMade").read[String] and
                                                            (JsPath \ "threePointsMade").read[String] and
                                                            (JsPath \ "freeThrowsMade").read[String] and
                                                            (JsPath \ "totalRebounds").read[String] and
                                                            (JsPath \ "offensiveRebounds").read[String] and
                                                            (JsPath \ "assists").read[String] and
                                                            (JsPath \ "personalFouls").read[String] and
                                                            (JsPath \ "steals").read[String] and
                                                            (JsPath \ "turnovers").read[String] and
                                                            (JsPath \ "blockedShots").read[String] and
                                                            (JsPath \ "points").read[String]
                                                          )(PlayerTotals.apply _)


  def loadBoxScore[T](url: String, f: BoxScore => T = dumpBoxScore): Future[Either[Seq[(JsPath, Seq[ValidationError])], T]] = {
    WS.url(url).get().map(unwrapJson).map(s => {
      Json.fromJson[BoxScore](Json.parse(s)) match {
        case JsSuccess(boxScore, _) => Right(f(boxScore))
        case JsError(errors) => Left(errors)
      }
    })
  }

  val dumpBoxScore: BoxScore => Unit = showBoxScore(_, System.out)


  def showBoxScore(b: BoxScore, out: PrintStream): Unit = {
    out.println(b.meta.description + " " + b.teams(0).teamId + " v. " + b.teams(1).teamId)
  }

  def unwrapJson(ws: WSResponse): String = {
    ws.body.replaceFirst("^\\s*callbackWrapper\\(", "").replaceFirst("\\);$", "")
  }

  def main(args: Array[String]) {
    test()
  }

  def test() {
    val testInput: String = """
  {
        "meta" : {
          "title": "Men's Basketball Boxscores",
          "description": "Boxscores for Washington St. vs California",
          "sport": "basketball-men",
          "division": "d1",
          "gametype": "Regular Season",
          "status" : "Final",
          "period": "OT",
          "minutes": "0",
          "seconds" : "00",
          "teams":[
        {

          "homeTeam": "true",
          "id": "2359",
          "seoName": "washington-st",
          "sixCharAbbr": "WASHST",
          "shortName": "Washington St.",
          "nickName": "Cougars",
          "color": "#981e32"

        },
        {

          "homeTeam": "false",
          "id": "2090",
          "seoName": "california",
          "sixCharAbbr": "CAL",
          "shortName": "California",
          "nickName": "Golden Bears",
          "color": "#00325b"

        }
          ]
        },

        "teams":[

        {
          "teamId":2090,
          "playerHeader":
            {
              "position": "POS",
              "minutesPlayed": "MIN",
              "fieldGoalsMade": "FGM-A",
              "threePointsMade": "3PM-A",
              "freeThrowsMade": "FTM-A",
              "totalRebounds": "REB",
              "offensiveRebounds": "OREB",
              "assists": "AST",
              "personalFouls": "PF",
              "steals": "ST",
              "turnovers": "TO",
              "blockedShots": "BLK",
              "points": "PTS"
            },
          "playerStats" : [

          {

            "firstName":"David",
            "lastName":"Kravish",
            "position":"F",
            "minutesPlayed":"40",
            "fieldGoalsMade":"7-11",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"6",
            "offensiveRebounds":"2",
            "assists":"0",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"1",
            "points":"14"

          },
          {

            "firstName":"Richard",
            "lastName":"Solomon",
            "position":"F",
            "minutesPlayed":"34",
            "fieldGoalsMade":"5-7",
            "threePointsMade":"0-0",
            "freeThrowsMade":"5-6",
            "totalRebounds":"11",
            "offensiveRebounds":"2",
            "assists":"0",
            "personalFouls":"5",
            "steals":"0",
            "turnovers":"4",
            "blockedShots":"0",
            "points":"15"

          },
          {

            "firstName":"Justin",
            "lastName":"Cobbs",
            "position":"G",
            "minutesPlayed":"39",
            "fieldGoalsMade":"6-11",
            "threePointsMade":"1-1",
            "freeThrowsMade":"9-11",
            "totalRebounds":"4",
            "offensiveRebounds":"1",
            "assists":"7",
            "personalFouls":"3",
            "steals":"1",
            "turnovers":"1",
            "blockedShots":"0",
            "points":"22"

          },
          {

            "firstName":"Tyrone",
            "lastName":"Wallace",
            "position":"G",
            "minutesPlayed":"36",
            "fieldGoalsMade":"2-8",
            "threePointsMade":"2-4",
            "freeThrowsMade":"1-2",
            "totalRebounds":"6",
            "offensiveRebounds":"0",
            "assists":"1",
            "personalFouls":"2",
            "steals":"1",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"7"

          },
          {

            "firstName":"Ricky",
            "lastName":"Kreklow",
            "position":"G",
            "minutesPlayed":"23",
            "fieldGoalsMade":"3-10",
            "threePointsMade":"2-5",
            "freeThrowsMade":"0-0",
            "totalRebounds":"3",
            "offensiveRebounds":"2",
            "assists":"1",
            "personalFouls":"1",
            "steals":"1",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"8"

          },
          {

            "firstName":"Sam",
            "lastName":"Singer",
            "position":"G",
            "minutesPlayed":"16",
            "fieldGoalsMade":"0-0",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"2",
            "offensiveRebounds":"1",
            "assists":"1",
            "personalFouls":"2",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"0"

          },
          {

            "firstName":"Jordan",
            "lastName":"Mathews",
            "position":"G",
            "minutesPlayed":"15",
            "fieldGoalsMade":"4-7",
            "threePointsMade":"3-4",
            "freeThrowsMade":"1-2",
            "totalRebounds":"1",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"1",
            "blockedShots":"0",
            "points":"12"

          },
          {

            "firstName":"Jabari",
            "lastName":"Bird",
            "position":"G",
            "minutesPlayed":"10",
            "fieldGoalsMade":"1-5",
            "threePointsMade":"0-1",
            "freeThrowsMade":"0-0",
            "totalRebounds":"0",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"0",
            "steals":"0",
            "turnovers":"1",
            "blockedShots":"0",
            "points":"2"

          },
          {

            "firstName":"Kameron",
            "lastName":"Rooks",
            "position":"C",
            "minutesPlayed":"7",
            "fieldGoalsMade":"0-1",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"2",
            "offensiveRebounds":"1",
            "assists":"0",
            "personalFouls":"2",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"0"

          },
          {

            "firstName":"Christian",
            "lastName":"Behrens",
            "position":"F",
            "minutesPlayed":"5",
            "fieldGoalsMade":"0-0",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"0",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"0"

          }
          ],
          "playerTotals" : {

            "fieldGoalsMade":"28-60",
            "threePointsMade":"8-15",
            "freeThrowsMade":"16-21",
            "totalRebounds":"38",
            "offensiveRebounds":"11",
            "assists":"10",
            "personalFouls":"18",
            "steals":"3",
            "turnovers":"7",
            "blockedShots":"1",
            "points":"80",

            "fieldGoalPercentage":"46.70%",
            "threePointPercentage":"53.30%",
            "freeThrowPercentage":"76.20%"


          }
        }
        ,

        {
          "teamId":2359,
          "playerHeader":
            {
              "position": "POS",
              "minutesPlayed": "MIN",
              "fieldGoalsMade": "FGM-A",
              "threePointsMade": "3PM-A",
              "freeThrowsMade": "FTM-A",
              "totalRebounds": "REB",
              "offensiveRebounds": "OREB",
              "assists": "AST",
              "personalFouls": "PF",
              "steals": "ST",
              "turnovers": "TO",
              "blockedShots": "BLK",
              "points": "PTS"
            },
          "playerStats" : [

          {

            "firstName":"D.J.",
            "lastName":"Shelton",
            "position":"F",
            "minutesPlayed":"42",
            "fieldGoalsMade":"5-10",
            "threePointsMade":"2-4",
            "freeThrowsMade":"6-6",
            "totalRebounds":"19",
            "offensiveRebounds":"4",
            "assists":"3",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"1",
            "points":"18"

          },
          {

            "firstName":"Junior",
            "lastName":"Longrus",
            "position":"F",
            "minutesPlayed":"14",
            "fieldGoalsMade":"0-0",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"1",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"1",
            "blockedShots":"2",
            "points":"0"

          },
          {

            "firstName":"DaVonte",
            "lastName":"Lacy",
            "position":"G",
            "minutesPlayed":"44",
            "fieldGoalsMade":"10-20",
            "threePointsMade":"8-15",
            "freeThrowsMade":"11-11",
            "totalRebounds":"0",
            "offensiveRebounds":"0",
            "assists":"1",
            "personalFouls":"2",
            "steals":"1",
            "turnovers":"3",
            "blockedShots":"0",
            "points":"39"

          },
          {

            "firstName":"Ike",
            "lastName":"Iroegbu",
            "position":"G",
            "minutesPlayed":"16",
            "fieldGoalsMade":"1-4",
            "threePointsMade":"0-1",
            "freeThrowsMade":"1-2",
            "totalRebounds":"2",
            "offensiveRebounds":"1",
            "assists":"1",
            "personalFouls":"2",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"3"

          },
          {

            "firstName":"Que",
            "lastName":"Johnson",
            "position":"G",
            "minutesPlayed":"16",
            "fieldGoalsMade":"2-7",
            "threePointsMade":"1-4",
            "freeThrowsMade":"0-0",
            "totalRebounds":"2",
            "offensiveRebounds":"1",
            "assists":"0",
            "personalFouls":"1",
            "steals":"0",
            "turnovers":"2",
            "blockedShots":"0",
            "points":"5"

          },
          {

            "firstName":"Dexter",
            "lastName":"Kernich-Drew",
            "position":"G",
            "minutesPlayed":"30",
            "fieldGoalsMade":"2-4",
            "threePointsMade":"2-3",
            "freeThrowsMade":"0-0",
            "totalRebounds":"1",
            "offensiveRebounds":"0",
            "assists":"2",
            "personalFouls":"3",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"6"

          },
          {

            "firstName":"Jordan",
            "lastName":"Railey",
            "position":"C",
            "minutesPlayed":"28",
            "fieldGoalsMade":"0-1",
            "threePointsMade":"0-0",
            "freeThrowsMade":"1-2",
            "totalRebounds":"0",
            "offensiveRebounds":"0",
            "assists":"1",
            "personalFouls":"4",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"2",
            "points":"1"

          },
          {

            "firstName":"Royce",
            "lastName":"Woolridge",
            "position":"G",
            "minutesPlayed":"28",
            "fieldGoalsMade":"1-6",
            "threePointsMade":"0-1",
            "freeThrowsMade":"1-2",
            "totalRebounds":"3",
            "offensiveRebounds":"1",
            "assists":"1",
            "personalFouls":"2",
            "steals":"1",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"3"

          },
          {

            "firstName":"Josh",
            "lastName":"Hawkinson",
            "position":"F",
            "minutesPlayed":"7",
            "fieldGoalsMade":"0-0",
            "threePointsMade":"0-0",
            "freeThrowsMade":"1-2",
            "totalRebounds":"1",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"0",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"1"

          },
          {

            "firstName":"Will",
            "lastName":"DiIorio",
            "position":"F",
            "minutesPlayed":"0",
            "fieldGoalsMade":"0-0",
            "threePointsMade":"0-0",
            "freeThrowsMade":"0-0",
            "totalRebounds":"0",
            "offensiveRebounds":"0",
            "assists":"0",
            "personalFouls":"0",
            "steals":"0",
            "turnovers":"0",
            "blockedShots":"0",
            "points":"0"

          }
          ],
          "playerTotals" : {

            "fieldGoalsMade":"21-52",
            "threePointsMade":"13-28",
            "freeThrowsMade":"21-25",
            "totalRebounds":"29",
            "offensiveRebounds":"7",
            "assists":"9",
            "personalFouls":"16",
            "steals":"2",
            "turnovers":"6",
            "blockedShots":"5",
            "points":"76",

            "fieldGoalPercentage":"40.40%",
            "threePointPercentage":"46.40%",
            "freeThrowPercentage":"84.00%"


          }
        }

        ]
      }

                            """
    val result: JsResult[BoxScore] = Json.fromJson[BoxScore](Json.parse(testInput))
    print(result.toString)
  }
}
