package pl.combosolutions.backup.psm.jvm

import java.io.File.pathSeparator

import pl.combosolutions.backup.psm.programs.Program
import JVMUtils._

import JVMProgramArgs._

object JVMProgramArgs {

  def argumentsFor[T <: App](mainClass: Class[T], mainClassArguments: List[String]): List[String] = {
    val mainName = handleCompanionObjectClassCase(mainClass.getName)
    val classPath = classPathFor(mainClass).reduce(_ + pathSeparator + _)
    jvmArgsExceptDebug ++ List("-cp", classPath, mainName) ++ mainClassArguments
  }

  private def handleCompanionObjectClassCase(className: String) = {
    assert(className endsWith "$")
    className.substring(0, className.length - 1)
  }
}

case class JVMProgram[T <: App](
  mainClass:          Class[T],
  mainClassArguments: List[String]
) extends Program[JVMProgram[T]](
  javaExec.toString,
  argumentsFor(mainClass, mainClassArguments)
)
