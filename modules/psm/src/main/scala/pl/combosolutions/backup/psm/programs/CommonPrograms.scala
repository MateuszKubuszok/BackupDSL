package pl.combosolutions.backup.psm.programs

import pl.combosolutions.backup.psm.ExecutionContexts
import ExecutionContexts.Program.context
import Program._
import pl.combosolutions.backup.psm.ExecutionContexts

import scala.concurrent.Future
import scalaz.OptionT._
import scalaz.std.scalaFuture._

class ProgramAlias[T <: Program[T], U <: Program[U]](
  aliased: Program[U]) extends Program[T](
  aliased.name,
  aliased.arguments
) {

  override def run = (for {
    originalResult <- optionT[Future](execute(aliased))
  } yield Result[T](originalResult.exitValue, originalResult.stdout, originalResult.stderr)).run
}

case class GenericProgram(
  override val name: String,
  override val arguments: List[String]) extends Program[GenericProgram](name, arguments)
