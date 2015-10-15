package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.PlatformSpecificSpecification
import pl.combosolutions.backup.psm.programs.{ GenericProgram, ProgramResultTestHelper }
import pl.combosolutions.backup.psm.systems.{ PosixSystem, WindowsSystem }
import pl.combosolutions.backup.test.Tags.PlatformTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PlatformSpecificAdvisedTest
    extends PlatformSpecificSpecification
    with ElevationTestHelper
    with ProgramResultTestHelper {

  val testProgram: GenericProgram = operatingSystem match {
    case system: WindowsSystem => GenericProgram("cmd", List())
    case system: PosixSystem   => GenericProgram("ls", List())
    case _                     => ReportException onNotImplemented "Unknown platform"
  }

  "Current platform's elevator" should {

    "allows direct elevation" in {
      // given
      val program = elevationService elevateDirect testProgram

      // when
      val result = program.run

      // then
      result should beCorrectProgramResult
    } tag PlatformTest

    "allows remote elevation" in CleanedContext {
      // given
      val program = elevationService elevateRemote (testProgram, ElevationTestCleaner)

      // when
      val result = program.run
      Await.result(result, Duration.Inf)

      // then
      result should beCorrectProgramResult
    } tag PlatformTest
  }
}
