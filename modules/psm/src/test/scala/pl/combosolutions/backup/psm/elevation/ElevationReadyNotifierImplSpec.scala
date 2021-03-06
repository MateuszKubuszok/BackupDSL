package pl.combosolutions.backup.psm.elevation

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class ElevationReadyNotifierImplSpec extends Specification with Mockito {

  "ElevationReadyNotifierImpl" should {

    "notify about successful initialization" in new TestContext {
      // given
      // when
      notifier.notifyReady

      // then
      there was one(success).apply()
    } tag UnitTest

    "notify about failed initialization" in new TestContext {
      // given
      // when
      notifier.notifyFailure

      // then
      there was one(failure).apply()
    } tag UnitTest
  }

  trait TestContext extends Scope {

    val success = mock[ElevationReadyNotifier.ReadyListener]
    val failure = mock[ElevationReadyNotifier.FailureListener]
    val notifier: ElevationReadyNotifier = new ElevationReadyNotifierImpl(success, failure)
  }
}
