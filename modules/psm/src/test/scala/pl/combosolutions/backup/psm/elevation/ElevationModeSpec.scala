package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Cleaner, Result }
import pl.combosolutions.backup.psm.commands.TestCommand
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.test.Tags.UnitTest

class ElevationModeSpec extends Specification with Mockito {

  val program = GenericProgram("test", List())
  val expectedProgram = GenericProgram("returned", List())
  val command = TestCommand(Result(0, List("1"), List()))
  val expectedCommand = TestCommand(Result(0, List("2"), List()))

  "NotElevated" should {

    "not elevate passed program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = NotElevated

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual program
    } tag UnitTest
  }

  "DirectElevation" should {

    "create directly elevated program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = new DirectElevation with TestElevationServiceComponent
      (mode.testElevationService elevateDirect program) returns expectedProgram

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual expectedProgram
      there was one(mode.testElevationService).elevateDirect(===(program))
    } tag UnitTest
  }

  "RemoteElevation" should {

    "create remotely elevated command" in {
      // given
      val cleaner = new Cleaner {}
      val mode = new RemoteElevation with TestElevationServiceComponent
      mode.testElevationService.elevateRemote(command, cleaner) returns expectedCommand

      // when
      val result = mode(command, cleaner)

      // then
      result mustEqual expectedCommand
      there was one(mode.testElevationService).elevateRemote(===(command), ===(cleaner))
    } tag UnitTest

    "create remotely elevated program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = new RemoteElevation with TestElevationServiceComponent
      mode.testElevationService.elevateRemote(program, cleaner) returns expectedProgram

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual expectedProgram
      there was one(mode.testElevationService).elevateRemote(===(program), ===(cleaner))
    } tag UnitTest
  }

  "ElevateIfNeeded" should {

    "run ElevationMode from implicit context" in {
      // given
      import ElevateIfNeeded._
      implicit val mode = mock[ElevationMode]
      implicit val cleaner = new Cleaner {}
      mode.apply(===(command), ===(cleaner)) returns expectedCommand
      mode.apply(===(program), ===(cleaner)) returns expectedProgram

      // when
      val resultCommand = command.handleElevation
      val resultProgram = program.handleElevation

      // then
      resultCommand mustEqual expectedCommand
      resultProgram mustEqual expectedProgram
    } tag UnitTest
  }
}
