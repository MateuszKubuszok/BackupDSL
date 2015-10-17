package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class CleanerSpec extends Specification with Mockito {

  "Cleaner" should {

    "run al cleanup tasks when asked" in {
      // given
      var tasksCalled = 0
      val task1: Cleaner#CleanUp = () => tasksCalled += 1
      val task2: Cleaner#CleanUp = () => tasksCalled += 1
      val task3: Cleaner#CleanUp = () => tasksCalled += 1
      val cleaner = new TestCleaner

      // when
      cleaner addTask task1
      cleaner addTask task2
      cleaner addTask task3
      cleaner.clean

      // then
      tasksCalled mustEqual 3
    } tag UnitTest
  }

  class TestCleaner extends Cleaner {

    override def clean = super.clean
  }
}
