package pl.combosolutions.backup.dsl.internals.jvm

import java.io.File

import pl.combosolutions.backup.dsl.internals.jvm.JVMUtils._
import pl.combosolutions.backup.dsl.internals.jvm.JVMProgramArgs._
import pl.combosolutions.backup.dsl.internals.programs.Program

object JVMProgramArgs {

  def argumentsFor[T <: App](mainClass: Class[T], mainClassArguments: List[String]): List[String] = {
    // format: OFF
    val realMainClassName = mainClass.getName
    val mainName = if (realMainClassName endsWith "$") realMainClassName.substring(0, realMainClassName.length - 1)
                   else realMainClassName
    val classPath = classPathFor(mainClass).reduce(_ + File.pathSeparator + _)
    // format: ON
    jvmArgsExceptDebug ++ List("-cp", classPath, mainName) ++ mainClassArguments
  }
}

case class JVMProgram[T <: App](
  mainClass: Class[T],
  mainClassArguments: List[String]) extends Program[JVMProgram[T]](
  javaExec.toString,
  argumentsFor(mainClass, mainClassArguments)
)
