name := "backup-dsl"

organization := "pl.combosolutions"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions in ThisBuild ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps"
)

crossScalaVersions := Seq("2.11.6")

resolvers += Resolver.sonatypeRepo("public")

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies ++= Seq(
  // functional utils
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.typelevel" % "scalaz-contrib-210_2.11" % "0.2",
  // Apache common utils
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-lang3" % "3.4",
  // command line
  "com.github.scopt" %% "scopt" % "3.3.0",
  // logging
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  // testing
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.mockito" % "mockito-core" % "1.10.8" % "test",
  "org.specs2" %% "specs2" % "2.4.1" % "test",
  "org.specs2" %% "specs2-core" % "2.4.1" % "test",
  "org.specs2" %% "specs2-junit" % "2.4.1" % "test"
)

initialCommands := "import pl.combosoutions.backup.dsl._"
