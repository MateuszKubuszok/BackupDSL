import sbt._
import sbt.Keys._

trait Settings extends Dependencies {

  val commonSettings = Seq(
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
    	Resolver.sonatypeRepo("public"),
			Resolver.typesafeRepo("releases")
    ),

    libraryDependencies ++= mainDeps,
    libraryDependencies ++= testDeps map (_ % "test")
  )
}
