package pl.combosolutions.backup.psm.elevation

import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoElevationAvailable
import pl.combosolutions.backup.test.Tags.UnitTest

class ElevationServiceSpec extends Specification {

  "ElevationServiceComponentImpl" should {

    "return correct error message for no available implementation" in {
      // given
      // when
      val result = ElevationServiceComponentImpl.notFoundMessage

      // then
      result mustEqual NoElevationAvailable
    } tag UnitTest
  }
}
