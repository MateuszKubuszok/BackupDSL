package pl.combosolutions.backup.psm.programs

import pl.combosolutions.backup.{ ExecutionContexts, Result }
import ExecutionContexts.Program.context
import Program._

class ProgramAlias[T <: Program[T], U <: Program[U]](
  aliased: Program[U]
) extends Program[T](
  aliased.name,
  aliased.arguments
) {

  override def run = execute(aliased).asAsync map { originalResult =>
    Result[T](originalResult.exitValue, originalResult.stdout, originalResult.stderr)
  }
}

case class GenericProgram(
  override val name:      String,
  override val arguments: List[String]
) extends Program[GenericProgram](name, arguments)
