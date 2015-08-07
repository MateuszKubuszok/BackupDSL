package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals.jvm.JVMUtils._

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import Program._

class ProgramAlias[T <: Program[T], U <: Program[U]](
    aliased: Program[U]
  ) extends Program[T](
    aliased.name,
    aliased.arguments
  ) {

  override def run = (for {
    originalResult <- optionT[Future](execute(aliased))
  } yield Result[T](originalResult.exitValue, originalResult.stdout, originalResult.stderr)).run
}

case class GenericProgram(
  override val name: String,
  override val arguments: List[String]
) extends Program[GenericProgram](name, arguments)

case class JVMProgram[T <: App](
  mainClass: Class[T],
  override val arguments: List[String]
) extends Program[JVMProgram[T]](
  javaExe.toString,
  List(
    "-cp", classPathFor(mainClass),
    mainClass.getName
  ) ++ arguments
)
