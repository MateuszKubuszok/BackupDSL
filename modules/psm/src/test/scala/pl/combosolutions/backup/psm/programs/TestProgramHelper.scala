package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import pl.combosolutions.backup.AsyncResult

import scala.sys.process.Process

trait TestProgramHelper[T <: Program[T]] extends Mockito {
  self: Program[T] =>

  var result = AsyncResult some Result[T](0, List(), List())

  val process = mock[Process]

  override def run = result

  override def run2Kill = process
}
