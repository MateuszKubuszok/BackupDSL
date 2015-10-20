package pl.combosolutions.backup.psm.filesystem

import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoFileSystemAvailable
import pl.combosolutions.backup.test.Tags.UnitTest

class FileSystemServiceSpec extends Specification {

  "FileSystemServiceComponentImpl" should {

    "return correct error message for no available implementation" in {
      // given
      // when
      val result = FileSystemServiceComponentImpl.notFoundMessage

      // then
      result mustEqual NoFileSystemAvailable
    } tag UnitTest
  }
}
