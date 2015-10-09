package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.sys.process.{ Process, ProcessBuilder, ProcessLogger }

class ProgramSpec extends Specification with Mockito {

  "Program$" should {

    "create GenericProgram when applied as function" in new TestContext {
      // given
      val expected = program

      // when
      val result = programObj(name, arguments: _*)

      // then
      result mustEqual expected
    }

    "run process and return result" in new TestContext {
      // given
      val expected = Result[GenericProgram](0, List(), List())
      programObj.process.exitValue returns 0

      // when
      val result = programObj execute program

      // then
      result must beSome(expected).await
    }

    "run process and return failure" in new TestContext {
      // given
      programObj.process.exitValue throws new RuntimeException

      // when
      val result = programObj execute program

      // then
      result must beNone.await
    }

    "run process and return handler to it" in new TestContext {
      // given
      // when
      val result = programObj execute2Kill program

      // then
      result mustEqual programObj.process
    }
  }

  trait MockProcessHelper {
    self: ProgramExecutor =>

    val process = mock[Process]
    val builder = mock[ProcessBuilder]
    builder.run returns process
    builder.run(any[ProcessLogger]) returns process

    override def processFor(name: String, arguments: List[String]) = builder
  }

  trait TestContext extends Scope {

    val programObj = new ProgramExecutor with MockProcessHelper
    val name = "test-name"
    val arguments = List("test", "test")
    val program = GenericProgram(name, arguments)
  }
}
