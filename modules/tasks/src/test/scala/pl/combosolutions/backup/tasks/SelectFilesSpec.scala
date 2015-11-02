package pl.combosolutions.backup.tasks

import java.nio.file.{ Paths, Path }

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.{ Async, Cleaner }
import pl.combosolutions.backup.test.Tags.UnitTest

class SelectFilesSpec extends Specification with Mockito {

  "SelectFiles" should {

    "actions should be independent" in new TestContext {
      // given
      // when
      selectFiles.build

      // then
      there was one(child).backupSubTaskBuilder
      there was one(child).restoreSubTaskBuilder
    } tag UnitTest

    "actions should always succeed" in new TestContext {
      // given
      // when
      val task = selectFiles.build
      val backupResult = task.backup
      val restoreResult = task.restore

      // then
      backupResult must beSome.await
      restoreResult must beSome.await
    } tag UnitTest
  }

  class TestContext extends Scope {

    val parent = mock[TaskBuilder[Any, Any, List[Path], Any, Any, List[Path]]]

    val child = mock[TaskBuilder[List[Path], List[Path], Any, List[Path], List[Path], Any]]
    val childBackup = mock[SubTaskBuilder[List[Path], List[Path], Any]]
    val childRestore = mock[SubTaskBuilder[List[Path], List[Path], Any]]
    child.backupSubTaskBuilder returns childBackup
    child.restoreSubTaskBuilder returns childRestore
    childBackup.injectableProxy returns new SubTaskProxy[List[Path]](DependencyType.ParentDependent)
    childRestore.injectableProxy returns new SubTaskProxy[List[Path]](DependencyType.ParentDependent)

    val files = () => List("file1", "file2")
    val selectFiles = new SelectFiles[Any, List[Path], Any, List[Path]](files)
    selectFiles.setParent(parent)
    selectFiles.addChild(child)

    val action = (paths: List[Path]) => Async some paths
    val childBackupSubTask = new ParentDependentSubTask[List[Path], List[Path]](action, selectFiles.backupSubTaskBuilder.injectableProxy)
    val childRestoreSubTask = new ParentDependentSubTask[List[Path], List[Path]](action, selectFiles.restoreSubTaskBuilder.injectableProxy)
  }
}
