package pl.combosolutions.backup.psm.elevation.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.elevation.{ RemoteElevatorProgram, RemoteElevation, ElevationService, TestElevationFacadeComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.{ Program, GenericProgram }

class LinuxElevationSpec extends Specification with Mockito {

  val component = new GKSudoElevationServiceComponent with TestElevationFacadeComponent
  val service = component.elevationService
  val program = GenericProgram("test-name", List("test-args"))

  "GKSudoElevationService" should {

    "elevate directly using DirectElevationProgram" in {
      // given
      val expectedName = "gksudo"
      val expectedArgs = List("-m", "BackupDSL elevation runner", "--") ++ List(program.name) ++ program.arguments

      // when
      val result: Program[GenericProgram] = service elevateDirect program
      val resultAsGeneric = result.asGeneric

      // then
      resultAsGeneric.name mustEqual expectedName
      resultAsGeneric.arguments mustEqual expectedArgs
    }

    "elevate remotely using RemoteElevationProgram" in {
      // given
      val expected = program
      val cleaner = new Cleaner {}

      // when
      val result = service elevateRemote (program, cleaner)
      val elevated = result.asInstanceOf[RemoteElevatorProgram[GenericProgram]].program

      // then
      elevated mustEqual expected
    }
  }
}
