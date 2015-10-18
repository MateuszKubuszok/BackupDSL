package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class AsyncTransformerSpec extends Specification with Mockito {

  "AsyncTransformer" should {

    "allow call to flatMap with implicits" in {
      // given
      val value = 1234567890
      val async = Async some value
      val action: (Int) => Async[String] = in => Async some in.toString
      val expected = value.toString

      // when
      val result = async.asAsync flatMap action

      // then
      result must beSome(expected).await
    } tag UnitTest

    "allow call to map with implicits" in {
      // given
      val value = 1234567890
      val async = Async some value
      val action: (Int) => String = _.toString
      val expected = value.toString

      // when
      val result = async.asAsync map action

      // then
      result must beSome(expected).await
    } tag UnitTest
  }
}
