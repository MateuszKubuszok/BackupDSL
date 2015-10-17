package pl.combosolutions.backup

import org.slf4j.Logger
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.test.Tags.UnitTest

class LoggerSpec extends Specification with Mockito {

  "LoggerWrapper" should {

    "log info" in new TestContext {
      // given
      // when
      wrapper info "test"

      // then
      there was one(wrapper.impl).info("test")
    } tag UnitTest

    "log debug" in new TestContext {
      // given
      // when
      wrapper debug "test"

      // then
      there was one(wrapper.impl).debug("    test")
    } tag UnitTest

    "log trace" in new TestContext {
      // given
      // when
      wrapper trace "test"

      // then
      there was one(wrapper.impl).trace("        test")
    } tag UnitTest

    "log warn" in new TestContext {
      // given
      // when
      wrapper warn "test"

      // then
      there was one(wrapper.impl).warn("    test")
    } tag UnitTest

    "log error" in new TestContext {
      // given
      val ex = new Throwable

      // when
      wrapper error "test"
      wrapper error ("test", ex)

      // then
      there was one(wrapper.impl).error("test")
      there was one(wrapper.impl).error("test", ex)
    } tag UnitTest
  }

  trait TestContext extends Scope {

    val wrapper = new LoggerWrapper(getClass) with TestLoggerWrapperHelper
  }

  trait TestLoggerWrapperHelper {
    self: LoggerWrapper =>

    override val impl = mock[Logger]
  }
}
