package pl.combosolutions.backup.dsl.internals.operations

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}

import Program._

object Program {

  def apply(name: String, arguments: String*) = GenericProgram(name, arguments.toList)

  type AsyncResult[U] = Future[Option[U]]
  def execute[T <: Program[T]](program: Program[T]): AsyncResult[Result[T]] = Future {
    Try {
      var stdout = mutable.MutableList[String]()
      var stderr = mutable.MutableList[String]()
      val logger = ProcessLogger(stdout += _, stderr += _)

      val exitValue = Process(program.name, program.arguments) run logger exitValue

      Result[T](exitValue, stdout toList, stderr toList)
    } match {
      case Success(result) => Some(result)
      case Failure(_)      => None
    }
  }
}


class Program[T <: Program[T]](val name:String, val arguments: List[String]) extends Serializable {

  def run         = execute(this)

  def runElevated = PlatformSpecific.current.elevate(this).run

  def digest[U](implicit interpreter: Result[T]#Interpreter[U]) = (for {
    result <- optionT[Future](run)
  } yield result.interpret(interpreter)).run

  def digestElevated[U](implicit interpreter: Result[T]#Interpreter[U]) =
    PlatformSpecific.current.elevate(this).digest[U](interpreter)

  def asGeneric: GenericProgram = GenericProgram(name, arguments)
}
