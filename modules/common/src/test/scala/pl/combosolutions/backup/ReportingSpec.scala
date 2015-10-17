package pl.combosolutions.backup

import org.slf4j.Logger
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.test.Tags.UnitTest

class ReportingSpec extends Specification with Mockito {

  "Reporting" should {

    "report information" in new TestContext {
      // given
      // when
      tested.reporter inform "test"

      // then
      there was one(tested.reporter.impl).info("test")
    } tag UnitTest

    "report more" in new TestContext {
      // given
      // when
      tested.reporter more "test"

      // then
      there was one(tested.reporter.impl).debug("    test")
    } tag UnitTest

    "report details" in new TestContext {
      // given
      // when
      tested.reporter details "test"

      // then
      there was one(tested.reporter.impl).trace("        test")
    } tag UnitTest

    "report error" in new TestContext {
      // given
      val ex = new Throwable

      // when
      tested.reporter error "test"
      tested.reporter error ("test", ex)

      // then
      there was one(tested.reporter.impl).error("        test")
      there was one(tested.reporter.impl).error("        test", ex)
    } tag UnitTest
  }

  trait TestContext extends Scope {

    val tested = new Reporting with TestReportingHelper
  }

  trait TestReportingHelper {
    self: Reporting =>

    override val reporter = new Reporter {

      override val impl = mock[Logger]
    }
  }
}
