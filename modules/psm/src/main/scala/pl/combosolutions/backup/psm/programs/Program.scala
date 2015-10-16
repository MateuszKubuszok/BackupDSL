package pl.combosolutions.backup.psm.programs

import pl.combosolutions.backup._
import ExecutionContexts.Program.context

import scala.collection.mutable
import scala.concurrent.Future
import scala.sys.process.{ Process, ProcessLogger }
import scala.util.{ Failure, Success, Try }
import scalaz.OptionT._
import scalaz.std.scalaFuture._

private[programs] trait ProgramExecutor extends Logging {

  def apply(name: String, arguments: String*) = GenericProgram(name, arguments.toList)

  def execute[T <: Program[T]](program: Program[T]): Async[Result[T]] = Async {
    Try {
      Program.logger trace s"running  ${program.asGeneric.showCMD} and awaiting results"

      var stdout = mutable.MutableList[String]()
      var stderr = mutable.MutableList[String]()
      val logger = ProcessLogger(stdout += _, stderr += _)

      val exitValue = processFor(program.name, program.arguments) run logger exitValue ()

      Program.logger trace s"finished ${program.asGeneric.toString}"

      Result[T](exitValue, stdout toList, stderr toList)
    } match {
      case Success(result) => Some(result)
      case Failure(ex) =>
        Program.logger error ("execution failed", ex)
        None
    }
  }

  def execute2Kill[T <: Program[T]](program: Program[T]): Process = {
    logger trace s"running  ${program.asGeneric.showCMD}"
    processFor(program.name, program.arguments) run ()
  }

  protected def processFor(name: String, arguments: List[String]) = Process(name, arguments)
}

object Program extends ProgramExecutor

import Program._

class Program[T <: Program[T]](val name: String, val arguments: List[String]) extends Executable[T] {

  override def run: Async[Result[T]] = execute(this)

  def run2Kill: Process = execute2Kill(this)

  override def digest[U](implicit interpreter: Result[T]#Interpreter[U]): Async[U] = (for {
    result <- optionT[Future](run)
  } yield result.interpret(interpreter)).run

  def asGeneric: GenericProgram = GenericProgram(name, arguments)

  def showCMD: String = s"'$name' ${showArgs(arguments)}"

  // format: OFF
  private def showArgs(arguments: List[String]) =
    if (arguments.isEmpty) ""
    else arguments map ("'" + _ + "'") reduce (_ + " " + _)
  // format: ON
}
