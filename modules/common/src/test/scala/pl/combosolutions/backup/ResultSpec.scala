package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class ResultSpec extends Specification with Mockito {

  "Result" should {

    "be interpretable" in {
      // given
      implicit val interpreter: Result[String] => Int = _.exitValue
      val expected = 1234567890
      val result = Result[String](expected, List(), List())

      // when
      val interpretation = result.interpret[Int]

      // then
      interpretation mustEqual expected
    } tag UnitTest

    "be castable" in {
      // given
      val result = Result[String](0, List(), List())

      // when
      val specific = result.asSpecific[Integer]

      // then
      specific mustEqual result
    } tag UnitTest
  }
}
