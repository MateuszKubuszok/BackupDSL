package pl.combosolutions.backup.psm.jvm

import java.io.File.pathSeparator

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.jvm.JVMUtils._
import pl.combosolutions.backup.test.Tags.UnitTest

class JVMProgramSpec extends Specification with Mockito {

  "JVMProgram" should {

    "create program for passed class" in {
      // given
      val mainClass = TestApp.getClass
      val args = List("1", "2", "3")
      val expectedArgs = List(
        mainClass.getName.subSequence(0, mainClass.getName.size - 1)
      ) ::: args
      val expectedCP = List("-cp", JVMUtils classPathFor mainClass reduce (_ + pathSeparator + _))
      val expectedJVMargs = JVMUtils.jvmArgsExceptDebug

      // when
      val program = JVMProgram(mainClass, args).asGeneric

      // then
      program.name mustEqual javaExec.toString
      program.arguments.drop(program.arguments.size - expectedArgs.size) mustEqual expectedArgs
      program.arguments.drop(program.arguments.size - expectedArgs.size - 2).take(2) mustEqual expectedCP
      program.arguments.take(expectedJVMargs.size) mustEqual expectedJVMargs
    } tag UnitTest
  }

}

object TestApp extends App
