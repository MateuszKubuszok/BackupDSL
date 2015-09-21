package pl.combosolutions.backup.psm.jvm

import java.io.File

import JVMUtils._
import JVMProgramArgs._
import pl.combosolutions.backup.psm.programs.Program

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
