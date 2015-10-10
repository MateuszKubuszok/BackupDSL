package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import pl.combosolutions.backup.{ Async, Result }

import scala.sys.process.Process

trait TestProgramHelper[T <: Program[T]] extends Mockito {
  self: Program[T] =>

  var result = Async some Result[T](0, List(), List())

  val process = mock[Process]

  override def run = result

  override def run2Kill = process
}
