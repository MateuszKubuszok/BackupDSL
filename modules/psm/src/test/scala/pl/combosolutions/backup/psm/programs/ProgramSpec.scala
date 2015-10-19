package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Result
import pl.combosolutions.backup.psm.jvm.JVMUtils.javaExec
import pl.combosolutions.backup.psm.jvm.{ JVMUtils, JVMProgram }
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.sys.process.{ ProcessIO, Process, ProcessBuilder, ProcessLogger }

class ProgramSpec extends Specification with Mockito {

  "Program$" should {

    "create GenericProgram when applied as function" in new CompanionObjectTestContext {
      // given
      val expected = program

      // when
      val result = programObj(name, arguments: _*)

      // then
      result mustEqual expected
    } tag UnitTest

    "run process and return result" in new CompanionObjectTestContext {
      // given
      val expected = Result[GenericProgram](0, List("test1"), List("test2"))
      programObj.process.exitValue returns 0
      programObj.outline = Some("test1")
      programObj.errline = Some("test2")

      // when
      val result = programObj execute program

      // then
      result must beSome(expected).await
    } tag UnitTest

    "run process and return failure" in new CompanionObjectTestContext {
      // given
      programObj.process.exitValue throws new RuntimeException

      // when
      val result = programObj execute program

      // then
      result must beNone.await
    } tag UnitTest

    "run process and return handler to it" in new CompanionObjectTestContext {
      // given
      // when
      val result = programObj execute2Kill program

      // then
      result mustEqual programObj.process
    } tag UnitTest
  }

  "Program" should {

    "digest result" in new ClassTestContext {
      // given
      implicit val interpreter: Result[GenericProgram]#Interpreter[String] = { _.toString }
      val expected = program.rawResult.toString

      // when
      val result = program.digest[String]

      // then
      result must beSome(expected).await
    } tag UnitTest

    "kill program" in new ClassTestContext {
      // given
      val program2Kill = new Program[GenericProgram](javaExec.toString, List("-version"))

      // when
      val result = program2Kill.run2Kill

      // then
      result.destroy must not(throwA)
    } tag UnitTest

    "convert into GenericProgram" in new ClassTestContext {
      // given
      val expected = GenericProgram(name, arguments)

      // when
      val result = program.asGeneric

      // then
      result mustEqual expected
    } tag UnitTest

    "show CMD" in new ClassTestContext {
      // given
      val expected1 = "'test-name' 'test' 'test'"
      val expected2 = "'test-name'"

      // when
      val result1 = program.showCMD
      val result2 = program2.showCMD

      // then
      result1 mustEqual expected1
      result2 mustEqual expected2
    } tag UnitTest
  }

  trait MockProcessHelper {
    self: ProgramExecutor =>

    var outline: Option[String] = None
    var errline: Option[String] = None
    val process = mock[Process]
    val builder = new ProcessBuilder {

      override def run(): Process = process

      override def run(logger: ProcessLogger): Process = {
        outline foreach (logger.out(_))
        errline foreach (logger.err(_))
        process
      }

      override def !! : String = ???

      override def #||(other: ProcessBuilder): ProcessBuilder = ???

      override def #&&(other: ProcessBuilder): ProcessBuilder = ???

      override def !!<(log: ProcessLogger): String = ???

      override def !!< : String = ???

      override def lineStream_! : Stream[String] = ???

      override def lineStream_!(log: ProcessLogger): Stream[String] = ???

      override def ###(other: ProcessBuilder): ProcessBuilder = ???

      override def #|(other: ProcessBuilder): ProcessBuilder = ???

      override def run(io: ProcessIO): Process = ???

      override def run(connectInput: Boolean): Process = ???

      override def run(log: ProcessLogger, connectInput: Boolean): Process = ???

      override def !<(log: ProcessLogger): Int = ???

      override def !< : Int = ???

      override def !!(log: ProcessLogger): String = ???

      override def hasExitValue: Boolean = ???

      override def canPipeTo: Boolean = ???

      override def !(log: ProcessLogger): Int = ???

      override def ! : Int = ???

      override def lineStream(log: ProcessLogger): Stream[String] = ???

      override def lineStream: Stream[String] = ???

      override protected def toSink: ProcessBuilder = ???

      override protected def toSource: ProcessBuilder = ???
    }

    override def processFor(name: String, arguments: List[String]) = builder
  }

  trait CompanionObjectTestContext extends Scope {

    val programObj = new ProgramExecutor with MockProcessHelper
    val name = "test-name"
    val arguments = List("test", "test")
    val program = GenericProgram(name, arguments)
  }

  trait ClassTestContext extends Scope {

    val name = "test-name"
    val arguments = List("test", "test")
    val program = new Program[GenericProgram](name, arguments) with TestProgramHelper[GenericProgram]
    val program2 = new Program[GenericProgram](name, List()) with TestProgramHelper[GenericProgram]
  }
}
