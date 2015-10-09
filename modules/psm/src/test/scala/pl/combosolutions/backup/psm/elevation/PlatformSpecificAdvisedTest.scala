package pl.combosolutions.backup.psm.elevation

import org.specs2.mutable.Specification
import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.psm.systems.{ PosixSystem, WindowsSystem }
import pl.combosolutions.backup.test.{ ElevationTestHelper, ProgramResultTestHelper }
import pl.combosolutions.backup.test.Tags.PlatformTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PlatformSpecificAdvisedTest
    extends Specification
    with ElevationTestHelper
    with ProgramResultTestHelper
    with ComponentsHelper {

  sequential // gksudo lock causes failure when some process already grabbed it

  val testProgram: GenericProgram = operatingSystem match {
    case system: WindowsSystem => GenericProgram("cmd", List())
    case system: PosixSystem => GenericProgram("ls", List())
    case _ => ReportException onNotImplemented "Unknown platform"
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
