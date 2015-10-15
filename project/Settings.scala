import com.typesafe.sbt.SbtScalariform._
import sbt.Defaults.testTasks
import sbt.TestFrameworks.Specs2
import sbt.Tests.Argument
import sbt._
import sbt.Keys._

import Settings._
import scalariform.formatter.preferences._
import scoverage.ScoverageKeys._
import scoverage.ScoverageSbtPlugin

object Settings extends Dependencies {

  private val platformTestTag = TestTag.PlatformTest
  val PlatformTest = config(platformTestTag) extend Test describedAs "Runs dangerous (!!!) platform-specific tests"

  private val functionalTestTag = TestTag.FunctionalTest
  val FunctionalTest = config(functionalTestTag) extend Test describedAs "Runs only functional tests"

  private val unitTestTag = TestTag.UnitTest
  val UnitTest = config(unitTestTag) extend Test describedAs "Runs only unit tests"

  private val disabledTestTag = TestTag.DisabledTest

  private val customSettings = Seq(
    organization := "pl.combosolutions",
    version := "0.2.0-SNAPSHOT",

    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-Ywarn-dead-code",
      "-Ywarn-infer-any",
      "-Ywarn-unused-import"
    ),

    resolvers ++= commonResolvers,

    libraryDependencies ++= mainDeps,
    libraryDependencies ++= testDeps map (_ % "test"),

    testOptions in Test += excludeTags(platformTestTag, disabledTestTag),
    coverageEnabled := true,

    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(IndentLocalDefs, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
  )

  private val commonSettings = scalariformSettings ++ customSettings

  private def excludeTags(tags: String*) = Argument(Specs2, "exclude", tags.reduce(_ + "," + _))
  private def includeTags(tags: String*) = Argument(Specs2, "include", tags.reduce(_ + "," + _))
  private def sequential = Argument(Specs2, "sequential")

  abstract class Configurator(project: Project, config: Configuration, tag: String) {

    protected def configure = project.
      configs(config).
      settings(inConfig(config)(testTasks): _*).
      settings(testOptions in config := Seq(includeTags(tag))).
      settings(libraryDependencies ++= testDeps map (_ % tag)).
      enablePlugins(ScoverageSbtPlugin)

    protected def configureSequential = configure.
      settings(testOptions in config ++= Seq(sequential)).
      settings(parallelExecution in config := false)
  }
}

trait Settings {

  implicit class CommonConfigurator(project: Project) {

    def configureCommon = project.settings(commonSettings: _*)
  }

  implicit class PlatformConfigurator(project: Project)
    extends Configurator(project, PlatformTest, platformTestTag) {

    def configurePlatform = configureSequential
  }

  implicit class FunctionalConfigurator(project: Project)
    extends Configurator(project, FunctionalTest, functionalTestTag) {

    def configureFunctional = configure
  }

  implicit class UnitConfigurator(project: Project)
    extends Configurator(project, UnitTest, unitTestTag) {

    def configureUnit = configure
  }
}
