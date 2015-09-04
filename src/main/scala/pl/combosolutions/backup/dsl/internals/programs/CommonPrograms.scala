package pl.combosolutions.backup.dsl.internals.programs

import java.io.File
import java.lang.management.ManagementFactory

import pl.combosolutions.backup.dsl.internals.jvm.JVMUtils._
import pl.combosolutions.backup.dsl.internals.programs.Program._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.OptionT._
import scalaz.std.scalaFuture._

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

object JVMProgram {
  def argumentsFor[T <: App](mainClass: Class[T], mainClassArguments: List[String]): List[String] = {
    val realMainClassName = mainClass.getName

    val mainName  = if (realMainClassName endsWith "$") realMainClassName.substring(0, realMainClassName.length-1)
                    else realMainClassName
    val jvmArgs   = stripDebugArgs(ManagementFactory.getRuntimeMXBean.getInputArguments.toList)
    val classPath = classPathFor(mainClass).reduce(_ + File.pathSeparator + _)
    jvmArgs ++ List("-cp", classPath, mainName) ++ mainClassArguments
  }

  private def stripDebugArgs(list: List[String]) = list.filterNot { arg =>
    val lcArg = arg.toLowerCase
    lcArg == "-xdebug" || lcArg == "-xrunjdwp" || lcArg.startsWith("-agentlib:jdwp")
  }
}

import pl.combosolutions.backup.dsl.internals.programs.JVMProgram._

case class JVMProgram[T <: App](
  mainClass: Class[T],
  mainClassArguments: List[String]
) extends Program[JVMProgram[T]](
  javaExec.toString,
  argumentsFor(mainClass, mainClassArguments)
)
