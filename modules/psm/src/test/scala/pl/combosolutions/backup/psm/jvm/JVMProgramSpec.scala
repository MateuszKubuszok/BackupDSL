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
        mainClass.getName.subSequence(0, mainClass.getName.length - 1)
      ) ::: args
      val expectedCP = List("-cp", JVMUtils classPathFor mainClass reduce (_ + pathSeparator + _))
      val expectedJVMargs = JVMUtils.jvmArgsExceptDebug
      val cpSize = expectedCP.size
      val commandSize = expectedArgs.size

      // when
      val program = JVMProgram(mainClass, args).asGeneric

      // then
      val beginningOfOwnArgs = program.arguments.size - commandSize
      val beginningOfCPArgs = beginningOfOwnArgs - cpSize
      program.name mustEqual javaExec.toString
      program.arguments drop beginningOfOwnArgs mustEqual expectedArgs
      program.arguments slice (beginningOfCPArgs, beginningOfCPArgs + cpSize) mustEqual expectedCP
      program.arguments take expectedJVMargs.size mustEqual expectedJVMargs
    } tag UnitTest
  }
}

object TestApp extends App
