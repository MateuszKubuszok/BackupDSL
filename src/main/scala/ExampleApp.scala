package example

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.elevation.ElevationFacade
import pl.combosolutions.backup.dsl.internals.operations.GenericProgram

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object ExampleApp extends App with Logging {
  logger info "create programs"

  val program1 = GenericProgram("ps", List("aux"))
  val program2 = GenericProgram("ls", List("-la"))

  logger info "create elevation"

  val elevation = new ElevationFacade

  logger info "run programs"

  var result1 = elevation runRemotely program1
  var result2 = elevation runRemotely program2


  var results = Future sequence Seq(result1, result2)

  logger info "await results"

  Await.result(results, Duration.Inf) map { _ match {
    case Some(result) =>
      println(s"exit code = ${result.exitValue}" )
      println(s"stdout    = ${result.stdout}" )
      println(s"stderr    = ${result.stderr}" )
    case None =>
      println("something failed")
  } }

  elevation close
}

/*
import pl.combosolutions.backup.dsl.Script

object ExampleApp extends Script("test script") {
  this addTask backupFiles("README.md")
}
*/
