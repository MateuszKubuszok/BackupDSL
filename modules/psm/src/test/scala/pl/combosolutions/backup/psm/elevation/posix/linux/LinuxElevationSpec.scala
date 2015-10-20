package pl.combosolutions.backup.psm.elevation.posix.linux

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.{ Cleaner, Result }
import pl.combosolutions.backup.psm.commands.TestCommand
import pl.combosolutions.backup.psm.elevation.{ RemoteElevatorCommand, RemoteElevatorProgram, TestElevationFacadeComponent }
import pl.combosolutions.backup.psm.programs.{ GenericProgram, Program }
import pl.combosolutions.backup.test.Tags.UnitTest

class LinuxElevationSpec extends Specification with Mockito {

  "GKSudoElevationService" should {

    "correctly calculate availability" in new GKSudoResolutionTestContext {
      // given
      // when
      val availabilityForGnome = serviceForGnome.elevationService.elevationAvailable
      val availabilityForKde = serviceForKde.elevationService.elevationAvailable
      val availabilityForShell = serviceForShell.elevationService.elevationAvailable
      val availabilityForWindows = serviceForWindows.elevationService.elevationAvailable

      // then
      availabilityForGnome mustEqual true
      availabilityForKde mustEqual true
      availabilityForShell mustEqual false
      availabilityForWindows mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new GKSudoResolutionTestContext {
      // given
      // when
      val priorityForGnome = serviceForGnome.elevationService.elevationPriority
      val priorityForKde = serviceForKde.elevationService.elevationPriority
      val priorityForShell = serviceForShell.elevationService.elevationPriority
      val priorityForWindows = serviceForWindows.elevationService.elevationPriority

      // then
      priorityForGnome mustEqual Preferred
      priorityForKde mustEqual Allowed
      priorityForShell mustEqual NotAllowed
      priorityForWindows mustEqual NotAllowed
    } tag UnitTest

    "elevate directly using DirectElevationProgram" in new GKSudoTestContext {
      // given
      val expectedName = "gksudo"
      val expectedArgs = List("-m", "BackupDSL elevation runner", "--") ++ List(program.name) ++ program.arguments

      // when
      val result: Program[GenericProgram] = service elevateDirect program
      val resultAsGeneric = result.asGeneric

      // then
      resultAsGeneric.name mustEqual expectedName
      resultAsGeneric.arguments mustEqual expectedArgs
    } tag UnitTest

    "elevate remotely using RemoteElevationCommand" in new GKSudoTestContext {
      // given
      val expected = command
      val cleaner = new Cleaner {}

      // when
      val result = service elevateRemote (command, cleaner)
      val elevated = result.asInstanceOf[RemoteElevatorCommand[TestCommand]].command

      // then
      elevated mustEqual expected
    } tag UnitTest

    "elevate remotely using RemoteElevationProgram" in new GKSudoTestContext {
      // given
      val expected = program
      val cleaner = new Cleaner {}

      // when
      val result = service elevateRemote (program, cleaner)
      val elevated = result.asInstanceOf[RemoteElevatorProgram[GenericProgram]].program

      // then
      elevated mustEqual expected
    } tag UnitTest
  }

  "KDESudoElevationService" should {

    "correctly calculate availability" in new KDESudoResolutionTestContext {
      // given
      // when
      val availabilityForGnome = serviceForGnome.elevationService.elevationAvailable
      val availabilityForKde = serviceForKde.elevationService.elevationAvailable
      val availabilityForWindows = serviceForShell.elevationService.elevationAvailable

      // then
      availabilityForGnome mustEqual true
      availabilityForKde mustEqual true
      availabilityForWindows mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new KDESudoResolutionTestContext {
      // given
      // when
      val priorityForGnome = serviceForGnome.elevationService.elevationPriority
      val priorityForKde = serviceForKde.elevationService.elevationPriority
      val priorityForWindows = serviceForShell.elevationService.elevationPriority

      // then
      priorityForGnome mustEqual Allowed
      priorityForKde mustEqual Preferred
      priorityForWindows mustEqual NotAllowed
    } tag UnitTest
  }

  trait GKSudoResolutionTestContext extends Scope {

    val serviceForGnome = new TestGKSudoElevationServiceComponent(DebianSystem, "gnome")
    val serviceForKde = new TestGKSudoElevationServiceComponent(DebianSystem, "kde")
    val serviceForShell = new TestGKSudoElevationServiceComponent(DebianSystem, "")
    val serviceForWindows = new TestGKSudoElevationServiceComponent(WindowsXPSystem, "")
    serviceForGnome.availableCommands.gkSudo returns true
    serviceForKde.availableCommands.gkSudo returns true
    serviceForShell.availableCommands.gkSudo returns false
  }

  trait GKSudoTestContext extends Scope {

    // format: OFF
    val component = new GKSudoElevationServiceComponent
      with TestElevationFacadeComponent
      with TestOperatingSystemComponent
      with TestAvailableCommandsComponent
    // format: ON
    val service = component.elevationService
    val command = TestCommand(Result(0, List(), List()))
    val program = GenericProgram("test-name", List("test-args"))
  }

  class TestGKSudoElevationServiceComponent(
    override val operatingSystem:       OperatingSystem,
    override val currentDesktopSession: String
  ) extends GKSudoElevationServiceComponent
      with TestElevationFacadeComponent
      with OperatingSystemComponent
      with TestAvailableCommandsComponent

  trait KDESudoResolutionTestContext extends Scope {

    val serviceForGnome = new TestKDESudoElevationServiceComponent(DebianSystem, "gnome")
    val serviceForKde = new TestKDESudoElevationServiceComponent(DebianSystem, "kde")
    val serviceForShell = new TestKDESudoElevationServiceComponent(DebianSystem, "")
    val serviceForWindows = new TestKDESudoElevationServiceComponent(WindowsXPSystem, "")
    serviceForGnome.availableCommands.kdeSudo returns true
    serviceForKde.availableCommands.kdeSudo returns true
    serviceForShell.availableCommands.kdeSudo returns false
  }

  class TestKDESudoElevationServiceComponent(
    override val operatingSystem:       OperatingSystem,
    override val currentDesktopSession: String
  ) extends KDESudoElevationServiceComponent
      with TestElevationFacadeComponent
      with OperatingSystemComponent
      with TestAvailableCommandsComponent
}
