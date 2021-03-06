package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import pl.combosolutions.backup.{ Async, Result }

import scala.sys.process.Process

trait TestProgramHelper[T <: Program[T]] extends Mockito {
  self: Program[T] =>

  val rawResult = Result[T](0, List(), List())

  var result = Async some rawResult

  val process = mock[Process]

  override def run = result

  override def run2Kill = process
}
