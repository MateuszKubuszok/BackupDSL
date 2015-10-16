package pl.combosolutions.backup.psm.commands

import pl.combosolutions.backup._
import ExecutionContexts.Command.context

trait Command[T <: Command[T]] extends Executable[T] {

  override def digest[U](implicit interpreter: Result[T]#Interpreter[U]): Async[U] = run.asAsync map (interpreter(_))
}
