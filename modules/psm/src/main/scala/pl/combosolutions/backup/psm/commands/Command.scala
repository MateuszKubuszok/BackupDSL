package pl.combosolutions.backup.psm.commands

import pl.combosolutions.backup._
import ExecutionContexts.Command.context

import scala.concurrent.Future
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait Command[T <: Command[T]] extends Executable[T] {

  override def digest[U](implicit interpreter: Result[T]#Interpreter[U]): Async[U] = (for {
    result <- optionT[Future](run)
  } yield result.interpret(interpreter)).run
}
