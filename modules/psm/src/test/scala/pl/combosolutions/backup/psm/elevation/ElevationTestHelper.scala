package pl.combosolutions.backup.psm.elevation

import org.specs2.mutable.{ BeforeAfter, Specification }
import pl.combosolutions.backup.Cleaner

trait ElevationTestHelper {
  self: Specification =>

  object CleanedContext extends BeforeAfter {

    def before: Any = {}
    def after: Any = ElevationTestCleaner.cleanup
  }

  object ElevationTestCleaner extends Cleaner {

    def cleanup() = clean
  }
}
