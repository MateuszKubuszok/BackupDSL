package pl.combosolutions.backup.dsl.internals.operations

import scalaz.OptionT._

import scala.collection.mutable
import scala.concurrent.Future
import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}

import Program._

object Program {

  def apply(name: String, arguments: String*) = new Program[Program](name, arguments.toList)

  type AsyncResult[U] = Future[Option[U]]
  def execute[T <: Program](program: Program[T]): AsyncResult[Result[T]] = Future {
    Try {
      var stdout = mutable.MutableList[String]()
      var stderr = mutable.MutableList[String]()
      val logger = ProcessLogger(stdout += _, stderr += _)

      val exitValue = Process(program.name, program.arguments) run logger exitValue

      Result[T](exitValue, stdout toList, stderr toList)
    } match {
      case Success(result) => Some(result)
      case Failure         => None
    }
  }
}


case class Program[T <: Program[T]](name:String, arguments: List[String]) {

  def run         = execute(this)

  def runElevated = PlatformSpecific.current.elevate(this).run

  def digest[U](implicit interpreter: Result[T]#Interpreter[U]) = (for {
    result <- optionT[Future](run)
  } yield result.interpret).run

  def digestElevated[U](implicit interpreter: Result[T]#Interpreter[U]) =
    PlatformSpecific.current.elevate(this).digest[U]
}
