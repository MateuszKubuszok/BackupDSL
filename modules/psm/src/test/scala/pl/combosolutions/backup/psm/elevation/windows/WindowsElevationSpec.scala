package pl.combosolutions.backup.psm.elevation.windows

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.GenericProgram

class WindowsElevationSpec extends Specification with Mockito {

  val program = GenericProgram("test", List())

  "EmptyElevationService" should {

    "only mock direct elevation" in {
      // given
      val expected = program

      // when
      val result = EmptyElevationService elevateDirect program

      // then
      result mustEqual expected
    }

    "only mock remote elevation" in {
      // given
      val cleaner = new Cleaner {}
      val expected = program

      // when
      val result = EmptyElevationService elevateRemote (program, cleaner)

      // then
      result mustEqual expected
    }
  }
}
