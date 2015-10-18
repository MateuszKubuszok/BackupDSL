package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Result
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.sys.process.{ Process, ProcessBuilder, ProcessLogger }

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
      val expected = Result[GenericProgram](0, List(), List())
      programObj.process.exitValue returns 0

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
      val expected = "'test-name' 'test' 'test'"

      // when
      val result = program.showCMD

      // then
      result mustEqual expected
    } tag UnitTest
  }

  trait MockProcessHelper {
    self: ProgramExecutor =>

    val process = mock[Process]
    val builder = mock[ProcessBuilder]
    builder.run returns process
    builder.run(any[ProcessLogger]) returns process

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
  }
}
