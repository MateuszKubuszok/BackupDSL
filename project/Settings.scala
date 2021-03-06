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
import org.scalastyle.sbt.ScalastylePlugin._

object Settings extends Dependencies {

  private val platformTestTag = TestTag.PlatformTest
  val PlatformTest = config(platformTestTag) extend Test describedAs "Runs dangerous (!!!) platform-specific tests"

  private val functionalTestTag = TestTag.FunctionalTest
  val FunctionalTest = config(functionalTestTag) extend Test describedAs "Runs only functional tests"

  private val unitTestTag = TestTag.UnitTest
  val UnitTest = config(unitTestTag) extend Test describedAs "Runs only unit tests"

  private val disabledTestTag = TestTag.DisabledTest

  private val commonSettings = Seq(
    organization := "pl.combosolutions",
    version := "0.2.0-SNAPSHOT",

    scalaVersion := scalaVersionUsed
  )

  private val rootSettings = commonSettings
  
  private val modulesSettings = scalariformSettings ++ commonSettings ++ Seq(
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
      "-Ywarn-unused-import",
      "-Xfatal-warnings",
      "-Xlint"
    ),

    resolvers ++= commonResolvers,

    libraryDependencies ++= mainDeps,
    libraryDependencies ++= testDeps map (_ % "test"),

    testOptions in Test += excludeTags(platformTestTag, disabledTestTag),
    coverageEnabled := true,

    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignArguments, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(IndentLocalDefs, false)
      .setPreference(PreserveSpaceBeforeArguments, true),

    scalastyleFailOnError := true
  )

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

  implicit class DataConfigurator(project: Project) {

    def setName(newName: String) = project.settings(name := newName)

    def setDescription(newDescription: String) = project.settings(description := newDescription)
    
    def setInitialCommand(newInitialCommand: String) =
      project.settings(initialCommands := s"pl.combosolutions.backup.$newInitialCommand")
  }
  
  implicit class RootConfigurator(project: Project) {

    def configureRoot = project.settings(rootSettings: _*)
  }
  
  implicit class ModuleConfigurator(project: Project) {

    def configureModule = project.settings(modulesSettings: _*)
  }

  implicit class PlatformTestConfigurator(project: Project)
    extends Configurator(project, PlatformTest, platformTestTag) {

    def configurePlatformTests = configureSequential
  }

  implicit class FunctionalTestConfigurator(project: Project)
    extends Configurator(project, FunctionalTest, functionalTestTag) {

    def configureFunctionalTests = configure
  }

  implicit class UnitTestConfigurator(project: Project)
    extends Configurator(project, UnitTest, unitTestTag) {

    def configureUnitTests = configure
  }
}
