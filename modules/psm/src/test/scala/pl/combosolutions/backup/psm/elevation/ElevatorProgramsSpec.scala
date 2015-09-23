package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.programs.{ GenericProgram, Result }
import pl.combosolutions.backup.psm.programs.posix.GrepFiles
import pl.combosolutions.backup.test.Tags.UnitTest
import pl.combosolutions.backup.test.{ Tags, AsyncResultSpecificationHelper }

class ElevatorProgramsSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  val program = GrepFiles("1", List("2", "3"))
  val programName = program.name
  val programArgs = program.arguments

  "DirectElevatorProgram" should {

    "wrap program as GenericProgram" in {
      // given
      val elevationName = "test1"
      val elevationArgs = List("test2", "test3")
      val elevationService = mock[ElevationService]
      val expectedName = elevationName
      val expectedArgs = elevationArgs ++ List(programName) ++ programArgs
      (elevationService.elevationCMD) returns elevationName
      (elevationService.elevationArgs) returns elevationArgs

      // when
      val elevated = DirectElevatorProgram(program, elevationService).asGeneric

      // then
      elevated.name mustEqual expectedName
      elevated.arguments mustEqual expectedArgs
    } tag UnitTest
  }

  "RemoteElevationProgram" should {

    "run programs via ElevationFacade" in {
      // given
      val elevationFacade = mock[ElevationFacade]
      val expected = Result[GenericProgram](0, List(), List())
      (elevationFacade runRemotely any[GenericProgram]) returns AsyncResult(expected)

      // when
      val result = RemoteElevatorProgram(program, elevationFacade).run

      // then
      await(result) must beSome(expected)
    } tag UnitTest
  }
}
