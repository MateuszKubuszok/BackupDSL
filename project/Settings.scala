import com.typesafe.sbt.SbtScalariform._
import sbt.TestFrameworks.Specs2
import sbt.Tests.Argument
import sbt._
import sbt.Keys._

trait Settings extends Dependencies {

  lazy val PlatformTestConfig = config("platform") extend(Test)
  lazy val FunctionalTestConfig = config("functional") extend(Test)
  lazy val UnitTestConfig = config("unit") extend(Test)

  val commonConfigs = Seq(PlatformTestConfig, FunctionalTestConfig, UnitTestConfig)

  val commonSettings =
    inConfig(PlatformTestConfig)(Defaults.testSettings) ++
    inConfig(FunctionalTestConfig)(Defaults.testSettings) ++
    inConfig(UnitTestConfig)(Defaults.testSettings) ++
    scalariformSettings ++
    Seq(
      organization := "pl.combosolutions",
      version := "0.1.0-SNAPSHOT",

      scalaVersion := "2.11.6",
      scalacOptions ++= Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps"
      ),

      resolvers ++= Seq(
        Resolver sonatypeRepo "public",
        Resolver typesafeRepo "releases"
      ),

      libraryDependencies ++= mainDeps,
      libraryDependencies ++= testDeps map (_ % "test"),

      testOptions in Test += Argument(Specs2, "exclude", s"${TestTag.PlatformTest},${TestTag.FunctionalTest}"),
      testOptions in PlatformTestConfig += Argument(Specs2, "include", TestTag.PlatformTest),
      testOptions in FunctionalTestConfig += Argument(Specs2, "include", TestTag.FunctionalTest),
      testOptions in UnitTestConfig += Argument(Specs2, "include", TestTag.UnitTest)
    )
}
