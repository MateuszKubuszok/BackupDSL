package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class ReportExceptionSpec extends Specification with Mockito {

  val message = "test-message"
  val exception = new Throwable

  "ReportException" should {

    "report illegal argument" in {
      // given
      val report: () => String = () => ReportException onIllegalArgumentOf message

      // when
      // then
      report() must throwA[IllegalArgumentException]
    } tag UnitTest

    "report illegal state" in {
      // given
      val report1: () => String = () => ReportException onIllegalStateOf message
      val report2: () => String = () => ReportException onIllegalStateOf (message, exception)

      // when
      // then
      report1() must throwA[IllegalStateException]
      report2() must throwA[IllegalStateException]
    } tag UnitTest

    "report not implemented" in {
      // given
      val report: () => String = () => ReportException onNotImplemented message

      // when
      // then
      report() must throwA[NotImplementedError]
    } tag UnitTest

    "report to do" in {
      // given
      val report: () => String = () => ReportException onToDoCodeIn getClass

      // when
      // then
      report() must throwA[NotImplementedError]
    } tag UnitTest
  }
}
