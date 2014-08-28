name := """reactfij"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
                             "org.mongodb" %% "casbah" % "2.7.3",
                             "org.jsoup" % "jsoup" %"1.7.3",
                             jdbc,
                             anorm,
                             cache,
                             ws
                           )

// For stable releases
resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

// For SNAPSHOT releases
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
