package pl.combosolutions.backup.psm.repositories

import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoRepositoriesAvailable
import pl.combosolutions.backup.test.Tags.UnitTest

class RepositoriesServiceSpec extends Specification {

  "RepositoriesServiceComponentImpl" should {

    "return correct error message for no available implementation" in {
      // given
      // when
      val result = RepositoriesServiceComponentImpl.notFoundMessage

      // then
      result mustEqual NoRepositoriesAvailable
    } tag UnitTest
  }
}
