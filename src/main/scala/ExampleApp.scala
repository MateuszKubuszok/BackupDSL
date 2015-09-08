package example

import java.io.File

import pl.combosolutions.backup.dsl.Script
import pl.combosolutions.backup.dsl.internals.programs.GenericProgram

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

// TODO: rewrite tasks in DSL as future-like monads

object ExampleApp extends Script("elevation test") {
  val classpath = System getProperty "java.class.path"
  val classpathEntries = (classpath split File.pathSeparator).toList
  logger info classpathEntries
}

/*
object ExampleApp extends Script("elevation test") {
  logger info "create programs"

  val program1 = GenericProgram("ps", List("aux"))
  val program2 = GenericProgram("ls", List("-la"))

  logger info "run programs"

  var result1 = elevate(program1).run
  var result2 = elevate(program2).run
  var results = Future sequence Seq(result1, result2)

  logger info "await results"

  Await.result(results, Duration.Inf) map { _ match {
    case Some(result) =>
      println(s"exit code = ${result.exitValue}" )
      println(s"stdout    = ${if (result.stdout.isEmpty) "empty" else result.stdout.reduce(_ + "\n" + _)}" )
      println(s"stderr    = ${if (result.stderr.isEmpty) "empty" else result.stderr.reduce(_ + "\n" + _)}" )
    case None =>
      println("something failed")
  } }
}
*/

/*
import pl.combosolutions.backup.dsl.Script

object ExampleApp extends Script("test script") {
  this addTask backupFiles("README.md")
}
*/
