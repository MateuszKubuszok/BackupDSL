package pl.combosolutions.backup.psm.repositories

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class RepositorySpec extends Specification with Mockito {

  "AptRepository" should {

    "serialize correctly" in {
      // given
      val repository1 = AptRepository(true, "test-url", "test-branch", List("area1", "area2"), List("32", "64"))
      val repository2 = AptRepository(false, "test-url", "test-branch", List(), List())

      // when
      val result1 = repository1.toString
      val result2 = repository2.toString

      // then
      result1 mustEqual "deb-src [arch=32,64] test-url test-branch area1 area2"
      result2 mustEqual "deb test-url test-branch"
    } tag UnitTest
  }
}
