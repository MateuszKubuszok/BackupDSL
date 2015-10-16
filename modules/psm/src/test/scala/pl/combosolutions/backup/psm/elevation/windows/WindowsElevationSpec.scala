package pl.combosolutions.backup.psm.elevation.windows

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Cleaner, Result }
import pl.combosolutions.backup.psm.commands.TestCommand
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.test.Tags.UnitTest

class WindowsElevationSpec extends Specification with Mockito {

  val service = EmptyElevationServiceComponent.elevationService
  val command = TestCommand(Result(0, List(), List()))
  val program = GenericProgram("test", List())

  "EmptyElevationService" should {

    "only mock direct program elevation" in {
      // given
      val expected = program

      // when
      val result = service elevateDirect program

      // then
      result mustEqual expected
    } tag UnitTest

    "only mock remote command elevation" in {
      // given
      val cleaner = new Cleaner {}
      val expected = command

      // when
      val result = service elevateRemote (command, cleaner)

      // then
      result mustEqual expected
    } tag UnitTest

    "only mock remote program elevation" in {
      // given
      val cleaner = new Cleaner {}
      val expected = program

      // when
      val result = service elevateRemote (program, cleaner)

      // then
      result mustEqual expected
    } tag UnitTest
  }
}
