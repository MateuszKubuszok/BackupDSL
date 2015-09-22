package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ElevationReadyNotifierImplSpec extends Specification with Mockito {

  "ElevationReadyNotifierImpl" should {

    "notify about successful initialization" in {
      // given
      val success = mock[ElevationReadyNotifier.ReadyListener]
      val failure = mock[ElevationReadyNotifier.FailureListener]
      val notifier: ElevationReadyNotifier = new ElevationReadyNotifierImpl(success, failure)

      // when
      notifier.notifyReady

      // then
      there was one(success).apply()
    }

    "notify about failed initialization" in {
      // given
      val success = mock[ElevationReadyNotifier.ReadyListener]
      val failure = mock[ElevationReadyNotifier.FailureListener]
      val notifier: ElevationReadyNotifier = new ElevationReadyNotifierImpl(success, failure)

      // when
      notifier.notifyFailure

      // then
      there was one(failure).apply()
    }
  }
}
