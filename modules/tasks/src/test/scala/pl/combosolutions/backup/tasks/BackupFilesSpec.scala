package pl.combosolutions.backup.tasks

import java.nio.file.{ Path, Paths }

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.filesystem.FileSystemService
import pl.combosolutions.backup.{ Async, Cleaner }
import pl.combosolutions.backup.test.Tags.UnitTest

class BackupFilesSpec extends Specification with Mockito {

  "BackupFiles" should {

    "actions should be parent dependent" in new TestContext {
      // given
      // when
      backupFiles.build

      // then
      there was one(child).backupSubTaskBuilder
      there was one(child).restoreSubTaskBuilder
    } tag UnitTest

    "actions should always succeed" in new TestContext {
      // given
      // when
      val task = backupFiles.build
      val backupResult = task.backup
      val restoreResult = task.restore

      // then
      backupResult must beSome.await
      restoreResult must beSome.await
      there was two(fileSystemService).copyFiles(any[List[(Path, Path)]])(any[ElevationMode], any[Cleaner])
    } tag UnitTest
  }

  class TestContext extends Scope {

    val parent = mock[TaskBuilder[List[Path], Any, List[Path], List[Path], Any, List[Path]]]
    val parentBackup = mock[SubTaskBuilder[List[Path], Any, List[Path]]]
    val parentRestore = mock[SubTaskBuilder[List[Path], Any, List[Path]]]
    parent.backupSubTaskBuilder returns parentBackup
    parent.restoreSubTaskBuilder returns parentRestore
    parentBackup.injectableProxy returns new SubTaskProxy[List[Path]](DependencyType.Independent)
    parentRestore.injectableProxy returns new SubTaskProxy[List[Path]](DependencyType.Independent)

    val child = mock[TaskBuilder[Any, List[Path], Any, Any, List[Path], Any]]
    val childBackup = mock[SubTaskBuilder[Any, List[Path], Any]]
    val childRestore = mock[SubTaskBuilder[Any, List[Path], Any]]
    child.backupSubTaskBuilder returns childBackup
    child.restoreSubTaskBuilder returns childRestore
    childBackup.injectableProxy returns new SubTaskProxy[Any](DependencyType.ParentDependent)
    childRestore.injectableProxy returns new SubTaskProxy[Any](DependencyType.ParentDependent)

    val components = mock[ComponentRegistry]
    val fileSystemService = mock[FileSystemService]
    components.fileSystemService returns fileSystemService
    fileSystemService.copyFiles(any[List[(Path, Path)]])(any[ElevationMode], any[Cleaner]) returns (Async some List(Paths get "file1", Paths get "file2"))

    val cleaner = new Cleaner {}
    implicit val settings: Settings = ImmutableSettings(
      cleaner    = cleaner,
      components = components
    )

    val backupFiles = new BackupFiles[Any, Any]
    backupFiles.setParent(parent)
    backupFiles.addChild(child)

    val files = () => Async some List(Paths get "file1", Paths get "file2")
    val parentBackupSubTask = new IndependentSubTask[List[Path]](files)
    val parentRestoreSubTask = new IndependentSubTask[List[Path]](files)
    parentBackup.injectableProxy setImplementation parentBackupSubTask
    parentRestore.injectableProxy setImplementation parentRestoreSubTask

    val action = (paths: List[Path]) => Async some paths
    val childBackupSubTask = new ParentDependentSubTask[Any, List[Path]](action, backupFiles.backupSubTaskBuilder.injectableProxy)
    val childRestoreSubTask = new ParentDependentSubTask[Any, List[Path]](action, backupFiles.restoreSubTaskBuilder.injectableProxy)
    childBackup.injectableProxy setImplementation childBackupSubTask
    childRestore.injectableProxy setImplementation childRestoreSubTask
  }
}
