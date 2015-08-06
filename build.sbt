name := "backup-dsl"

organization := "pl.combosolutions"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions in ThisBuild ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:postfixOps"
)

crossScalaVersions := Seq("2.11.2")

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.typelevel" % "scalaz-contrib-210_2.11" % "0.2",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "com.github.scopt" %% "scopt" % "3.3.0"
)

initialCommands := "import pl.combosoutions.backup.dsl._"
