package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.{ Async, Cleaner }
import pl.combosolutions.backup.test.Tags.UnitTest

class RootSpec extends Specification with Mockito {

  "Root" should {

    "actions should be child dependent" in new TestContext {
      // given
      // when
      root.build

      // then
      there was one(child).backupSubTaskBuilder
      there was one(child).restoreSubTaskBuilder
    } tag UnitTest

    "actions should succeed for successful children" in new TestContext {
      // given
      val action = () => Async some "unimportant"
      val backupSubTask = new IndependentSubTask[Any](action)
      val restoreSubTask = new IndependentSubTask[Any](action)
      childBackup.injectableProxy setImplementation backupSubTask
      childRestore.injectableProxy setImplementation restoreSubTask

      // when
      val task = root.build
      val backupResult = task.backup
      val restoreResult = task.restore

      // then
      backupResult must beSome.await
      restoreResult must beSome.await
    } tag UnitTest

    "actions should fail for failed children" in new TestContext {
      // given
      val action = () => Async failed new Throwable
      val backupSubTask = new IndependentSubTask[Any](action)
      val restoreSubTask = new IndependentSubTask[Any](action)
      childBackup.injectableProxy setImplementation backupSubTask
      childRestore.injectableProxy setImplementation restoreSubTask

      // when
      val task = root.build
      val backupResult = task.backup
      val restoreResult = task.restore

      // then
      backupResult must throwA.await
      restoreResult must throwA.await
    } tag UnitTest
  }

  class TestContext extends Scope {

    val child = mock[TaskBuilder[Any, Unit, Any, Any, Unit, Any]]
    val childBackup = mock[SubTaskBuilder[Any, Unit, Any]]
    val childRestore = mock[SubTaskBuilder[Any, Unit, Any]]
    child.backupSubTaskBuilder returns childBackup
    child.restoreSubTaskBuilder returns childRestore
    childBackup.injectableProxy returns new SubTaskProxy[Any](DependencyType.Independent)
    childRestore.injectableProxy returns new SubTaskProxy[Any](DependencyType.Independent)
    val cleaner = new Cleaner {}
    implicit val settings: Settings = ImmutableSettings(
      cleaner = cleaner
    )
    val root = new Root
    root.addChild(child)
  }
}
