package pl.combosolutions.backup.psm.systems

import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoOperatingSystemAvailable
import pl.combosolutions.backup.test.Tags.UnitTest

class OperatingSystemSpec extends Specification {

  "OperatingSystemComponentImpl" should {

    "return correct error message for no available implementation" in {
      // given
      // when
      val result = OperatingSystemComponentImpl.notFoundMessage

      // then
      result mustEqual NoOperatingSystemAvailable
    } tag UnitTest
  }
}
