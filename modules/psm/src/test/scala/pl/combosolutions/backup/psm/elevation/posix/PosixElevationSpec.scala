package pl.combosolutions.backup.psm.elevation.posix

import org.specs2.matcher.Scope
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.elevation.TestElevationFacadeComponent
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.test.Tags.UnitTest

class PosixElevationSpec extends Specification {

  "SudoElevationService" should {

    "correctly calculate availability" in new SudoResolutionTestContext {
      // given
      // when
      val availabilityForGnome = serviceForGnome.elevationService.elevationAvailable
      val availabilityForKde = serviceForKde.elevationService.elevationAvailable
      val availabilityForShell = serviceForShell.elevationService.elevationAvailable
      val availabilityForWindows = serviceForWindows.elevationService.elevationAvailable

      // then
      availabilityForGnome mustEqual true
      availabilityForKde mustEqual true
      availabilityForShell mustEqual true
      availabilityForWindows mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new SudoResolutionTestContext {
      // given
      // when
      val priorityForGnome = serviceForGnome.elevationService.elevationPriority
      val priorityForKde = serviceForKde.elevationService.elevationPriority
      val priorityForShell = serviceForShell.elevationService.elevationPriority
      val priorityForWindows = serviceForWindows.elevationService.elevationPriority

      // then
      priorityForGnome mustEqual Allowed
      priorityForKde mustEqual Allowed
      priorityForShell mustEqual OnlyAllowed
      priorityForWindows mustEqual NotAllowed
    } tag UnitTest
  }

  trait SudoResolutionTestContext extends Scope {

    val serviceForGnome = new TestSudoElevationServiceComponent(DebianSystem, "gnome")
    val serviceForKde = new TestSudoElevationServiceComponent(DebianSystem, "kde")
    val serviceForShell = new TestSudoElevationServiceComponent(DebianSystem, "")
    val serviceForWindows = new TestSudoElevationServiceComponent(Windows7System, "")
  }

  class TestSudoElevationServiceComponent(
    override val operatingSystem:       OperatingSystem,
    override val currentDesktopSession: String
  ) extends SudoElevationServiceComponent
      with TestElevationFacadeComponent
      with OperatingSystemComponent
      with TestAvailableCommandsComponent
}
