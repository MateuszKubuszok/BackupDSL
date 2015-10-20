package pl.combosolutions.backup.psm.elevation.windows

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Cleaner, Result }
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.commands.TestCommand
import pl.combosolutions.backup.psm.elevation.TestElevationFacadeComponent
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.test.Tags.UnitTest

class WindowsElevationSpec extends Specification with Mockito {

  "EmptyElevationService" should {

    "correctly calculate availability" in new EmptyElevationResolutionTestContext {
      // given
      // when
      val availabilityForOldWindows = serviceForOldWindows.elevationService.elevationAvailable
      val availabilityForNewWindows = serviceForNewWindows.elevationService.elevationAvailable
      val availabilityForLinux = serviceForLinux.elevationService.elevationAvailable

      // then
      availabilityForOldWindows mustEqual true
      availabilityForNewWindows mustEqual false
      availabilityForLinux mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new EmptyElevationResolutionTestContext {
      // given
      // when
      val priorityForOldWindows = serviceForOldWindows.elevationService.elevationPriority
      val priorityForNewWindows = serviceForNewWindows.elevationService.elevationPriority
      val priorityForLinux = serviceForLinux.elevationService.elevationPriority

      // then
      priorityForOldWindows mustEqual OnlyAllowed
      priorityForNewWindows mustEqual NotAllowed
      priorityForLinux mustEqual NotAllowed
    } tag UnitTest

    "only mock direct program elevation" in new EmptyElevationTestContext {
      // given
      val expected = program

      // when
      val result = service elevateDirect program

      // then
      result mustEqual expected
    } tag UnitTest

    "only mock remote command elevation" in new EmptyElevationTestContext {
      // given
      val cleaner = new Cleaner {}
      val expected = command

      // when
      val result = service elevateRemote (command, cleaner)

      // then
      result mustEqual expected
    } tag UnitTest

    "only mock remote program elevation" in new EmptyElevationTestContext {
      // given
      val cleaner = new Cleaner {}
      val expected = program

      // when
      val result = service elevateRemote (program, cleaner)

      // then
      result mustEqual expected
    } tag UnitTest
  }

  "UACElevationService" should {

    "correctly calculate availability" in new UACElevationResolutionTestContext {
      // given
      // when
      val availabilityForOldWindows = serviceForOldWindows.elevationService.elevationAvailable
      val availabilityForNewWindows = serviceForNewWindows.elevationService.elevationAvailable
      val availabilityForLinux = serviceForLinux.elevationService.elevationAvailable

      // then
      availabilityForOldWindows mustEqual false
      availabilityForNewWindows mustEqual true
      availabilityForLinux mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new UACElevationResolutionTestContext {
      // given
      // when
      val priorityForOldWindows = serviceForOldWindows.elevationService.elevationPriority
      val priorityForNewWindows = serviceForNewWindows.elevationService.elevationPriority
      val priorityForLinux = serviceForLinux.elevationService.elevationPriority

      // then
      priorityForOldWindows mustEqual NotAllowed
      priorityForNewWindows mustEqual OnlyAllowed
      priorityForLinux mustEqual NotAllowed
    } tag UnitTest
  }

  trait EmptyElevationResolutionTestContext extends Scope {

    val serviceForOldWindows = new TestEmptyElevationServiceComponent(Windows98System)
    val serviceForNewWindows = new TestEmptyElevationServiceComponent(WindowsXPSystem)
    val serviceForLinux = new TestEmptyElevationServiceComponent(DebianSystem)
  }

  trait EmptyElevationTestContext extends Scope {

    val service = EmptyElevationServiceComponent.elevationService
    val command = TestCommand(Result(0, List(), List()))
    val program = GenericProgram("test", List())
  }

  class TestEmptyElevationServiceComponent(override val operatingSystem: OperatingSystem)
    extends EmptyElevationServiceComponent
    with TestElevationFacadeComponent
    with OperatingSystemComponent

  trait UACElevationResolutionTestContext extends Scope {

    val serviceForOldWindows = new TestUACElevationServiceComponent(Windows98System)
    val serviceForNewWindows = new TestUACElevationServiceComponent(WindowsXPSystem)
    val serviceForLinux = new TestUACElevationServiceComponent(DebianSystem)
  }

  class TestUACElevationServiceComponent(override val operatingSystem: OperatingSystem)
    extends UACElevationServiceComponent
    with TestElevationFacadeComponent
    with OperatingSystemComponent
}
