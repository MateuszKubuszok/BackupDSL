package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class CommonProgramsSpec extends Specification with Mockito {

  "GenericProgram" should {

    "pass called program name and agruments without change" in {
      // given
      val name = "test-program"
      val arguments = List("test1", "test2")

      // when
      val program = GenericProgram(name, arguments)

      // then
      program.name mustEqual name
      program.arguments mustEqual arguments
    } tag UnitTest
  }

  "ProgramAlias" should {

    "redirect name and arguments properly" in {
      // given
      val arguments = List("test1", "test2")
      val expected = TestProgram(arguments ++ List("test3"))

      // when
      val alias = TestAlias(arguments)

      // then
      alias.name mustEqual expected.name
      alias.arguments mustEqual expected.arguments
    } tag UnitTest
  }

  case class TestProgram(args: List[String]) extends Program[TestProgram]("test", args)

  case class TestAlias(args: List[String]) extends ProgramAlias[TestAlias, TestProgram](TestProgram(args ++ List("test3")))
}
