package pl.combosolutions.backup.dsl.internals.programs

import pl.combosolutions.backup.dsl.Logging

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object Program extends Logging {

  def apply(name: String, arguments: String*) = GenericProgram(name, arguments.toList)

  type AsyncResult[U] = Future[Option[U]]
  def execute[T <: Program[T]](program: Program[T]): AsyncResult[Result[T]] = Future {
    Try {
      Program.logger trace s"running  ${program.asGeneric.showCMD} and awaiting results"

      var stdout = mutable.MutableList[String]()
      var stderr = mutable.MutableList[String]()
      val logger = ProcessLogger(stdout += _, stderr += _)

      val exitValue = Process(program.name, program.arguments) run logger exitValue

      Program.logger trace s"finished ${program.asGeneric.toString}"

      Result[T](exitValue, stdout toList, stderr toList)
    } match {
      case Success(result) => Some(result)
      case Failure(ex)     => Program.logger error("execution failed", ex)
                              None
    }
  }

  def execute2Kill[T <: Program[T]](program: Program[T]) = {
    logger trace s"running  ${program.asGeneric.showCMD}"
    Process(program.name, program.arguments).run
  }
}

import pl.combosolutions.backup.dsl.internals.programs.Program._

class Program[T <: Program[T]](val name:String, val arguments: List[String]) extends Serializable {

  def run      = execute(this)

  def run2Kill = execute2Kill(this)

  def digest[U](implicit interpreter: Result[T]#Interpreter[U]) = (for {
    result <- optionT[Future](run)
  } yield result.interpret(interpreter)).run

  def asGeneric: GenericProgram = GenericProgram(name, arguments)

  def showCMD: String = s"'$name' ${showArgs(arguments)}"

  private def showArgs(arguments: List[String]) = if (arguments.isEmpty) ""
                                                  else arguments map ("'" + _ + "'") reduce (_ + " " + _)
}
