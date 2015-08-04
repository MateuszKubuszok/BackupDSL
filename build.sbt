name := "backup-dsl"

organization := "pl.combosolutions"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.apache.commons" % "commons-lang3" % "3.4"
)

initialCommands := "import pl.combosoutions._"
