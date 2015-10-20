package pl.combosolutions.backup.psm.filesystem.posix

import org.specs2.matcher.Scope
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.test.Tags.UnitTest

class PosixFilesServiceSpec extends Specification {

  "PosixFilesService" should {

    "correctly calculate availability" in new PosixFilesServiceResolutionTestContext {
      // given
      // when
      val availabilityForPosix = serviceForPosix.filesService.filesAvailable
      val availabilityForWindows = serviceForWindows.filesService.filesAvailable

      // then
      availabilityForPosix mustEqual true
      availabilityForWindows mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new PosixFilesServiceResolutionTestContext {
      // given
      // when
      val availabilityForPosix = serviceForPosix.filesService.filesPriority
      val availabilityForWindows = serviceForWindows.filesService.filesPriority

      // then
      availabilityForPosix mustEqual OnlyAllowed
      availabilityForWindows mustEqual NotAllowed
    } tag UnitTest
  }

  trait PosixFilesServiceResolutionTestContext extends Scope {

    val serviceForPosix = new TestPosixFilesServiceComponent(DebianSystem)
    val serviceForWindows = new TestPosixFilesServiceComponent(Windows7System)
  }

  class TestPosixFilesServiceComponent(override val operatingSystem: OperatingSystem)
    extends PosixFilesServiceComponent
    with OperatingSystemComponent
}
