import com.typesafe.sbt.SbtScalariform._
import sbt.Defaults.testSettings
import sbt.TestFrameworks.Specs2
import sbt.Tests.Argument
import sbt._
import sbt.Keys._

trait Settings extends Dependencies {

  val platformTestTag = TestTag.PlatformTest
  val PlatformTest = config(platformTestTag) extend(Test)

  val functionalTestTag = TestTag.FunctionalTest
  val FunctionalTest = config(functionalTestTag) extend(Test)

  val unitTestTag = TestTag.UnitTest
  val UnitTest = config(unitTestTag) extend(Test)

  val projectSettings = Seq(
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

    testOptions in Test += excludeTags(platformTestTag)
  )

  val commonSettings = scalariformSettings ++ projectSettings

  private def excludeTags(tags: String*) = Argument(Specs2, "exclude", tags.reduce(_ + "," + _))
  private def includeTags(tags: String*) = Argument(Specs2, "include", tags.reduce(_ + "," + _))

  abstract class Configurator(project: Project, config: Configuration, tag: String) {

    protected def configure() = project.
      configs(config).
      settings(inConfig(config)(testSettings): _*).
      settings(testOptions := Seq(includeTags(tag)))
  }

  implicit class PlatformConfigurator(project: Project) extends Configurator(project, PlatformTest, platformTestTag) {

    def configurePlatform() = configure
  }

  implicit class FunctionalConfigurator(project: Project) extends Configurator(project, FunctionalTest, functionalTestTag) {

    def configureFunctional() = configure
  }

  implicit class UnitConfigurator(project: Project) extends Configurator(project, UnitTest, unitTestTag) {

    def configureUnit() = configure
  }
}
