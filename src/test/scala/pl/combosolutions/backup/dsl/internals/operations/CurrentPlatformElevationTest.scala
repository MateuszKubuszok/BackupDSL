package pl.combosolutions.backup.dsl.internals.operations

import org.specs2.mutable.{BeforeAfter, Specification}
import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.test.ProgramResultTestHelper
import pl.combosolutions.backup.dsl.test.Tags.CurrentPlatformTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CurrentPlatformElevationTest extends Specification with ProgramResultTestHelper {

  sequential // gksudo lock causes failure when some process already grabbed it

  val currentPlatform = PlatformSpecific.current

  val currentSystem   = OperatingSystem.current

  val testProgram     = if      (currentSystem.isWindows) GenericProgram("cmd", List())
                        else if (currentSystem.isPosix)   GenericProgram("ls", List())
                        else                              throw new NotImplementedError("Unknown platform")

  "Current platform's elevator" should {

    "allows direct elevation" in {
      val program = currentPlatform elevateDirect testProgram

      val result = program.run

      result should beCorrectProgramResult
    } tag (CurrentPlatformTest)

    "allows remote elevation" in CleanedContext {
      val program = currentPlatform elevateRemote(testProgram, ElevationTestCleaner)

      val result = program.run

      val w8 = Await.result(result, Duration.Inf)

      result should beCorrectProgramResult
    } tag (CurrentPlatformTest)
  }

  object CleanedContext extends BeforeAfter {
    def before: Any = {}
    def after:  Any = ElevationTestCleaner.cleanup
  }

  object ElevationTestCleaner extends Cleaner {
    def cleanup = clean
  }
}
