package pl.combosolutions.backup.psm.elevation.windows

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.test.Tags.UnitTest

class WindowsElevationSpec extends Specification with Mockito {

  val service = EmptyElevationServiceComponent.elevationService
  val program = GenericProgram("test", List())

  "EmptyElevationService" should {

    "only mock direct elevation" in {
      // given
      val expected = program

      // when
      val result = service elevateDirect program

      // then
      result mustEqual expected
    } tag UnitTest

    "only mock remote elevation" in {
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
