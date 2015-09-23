import sbt._
import Keys._

trait Dependencies {

  // functional utils
  val scalaz        = "org.scalaz" %% "scalaz-core" % "7.1.3"
  val scalazContrib = "org.typelevel" % "scalaz-contrib-210_2.11" % "0.2"

  // Apache common utils
  val commonsIo   = "commons-io" % "commons-io" % "2.4"
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.4"

  // command line
  val scopt = "com.github.scopt" %% "scopt" % "3.3.0"

  // logging
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"

  // testing
  val mockito    = "org.mockito" % "mockito-core" % "1.10.8"
  val spec2      = "org.specs2" %% "specs2" % "2.4.1"
  val spec2Core  = "org.specs2" %% "specs2-core" % "2.4.1"
  val spec2JUnit = "org.specs2" %% "specs2-junit" % "2.4.1"

  val mainDeps = Seq(scalaz, scalazContrib, commonsIo, commonsLang, scopt, logback)

  val testDeps = Seq(mockito, spec2, spec2Core, spec2JUnit)
}
