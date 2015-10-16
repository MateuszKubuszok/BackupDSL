package pl.combosolutions.backup.tasks

import java.nio.file.{ Files, Path, Paths }

import pl.combosolutions.backup.{ Async, Reporting }
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

object BackupFiles extends Reporting {

  val filesBackupDir = "files"

  private def backupAction(implicit withSettings: Settings): (List[Path]) => Async[List[Path]] = { files =>
    implicit val e = withSettings.withElevation
    implicit val c = withSettings.cleaner
    reporter inform s"Backing up files: $files"
    withSettings.components.fileSystemService copyFiles backupPaths(files)
  }

  private def restoreAction(implicit withSettings: Settings): (List[Path]) => Async[List[Path]] = { files =>
    implicit val e = withSettings.withElevation
    implicit val c = withSettings.cleaner
    reporter inform s"Restoring files: $files"
    withSettings.components.fileSystemService copyFiles restorePaths(files)
  }

  private def backupPaths(files: List[Path])(implicit withSettings: Settings) = files map { file =>
    val backup = file.toAbsolutePath
    val restore = Paths.get(withSettings.backupDir.toString, filesBackupDir, backup.hashCode.toString)
    (backup, restore)
  }

  private def restorePaths(files: List[Path])(implicit withSettings: Settings) = backupPaths(files) map (_.swap)

  class BackupSubTaskBuilder[ChildResult](implicit withSettings: Settings)
    extends ParentDependentSubTaskBuilder[List[Path], List[Path], ChildResult](backupAction)

  class RestoreSubTaskBuilder[ChildResult](implicit withSettings: Settings)
    extends ParentDependentSubTaskBuilder[List[Path], List[Path], ChildResult](restoreAction)
}

import BackupFiles._

class BackupFiles[CBR, CRR](implicit withSettings: Settings)
  extends TaskBuilder[List[Path], List[Path], CBR, List[Path], List[Path], CRR](
    new BackupSubTaskBuilder[CBR],
    new RestoreSubTaskBuilder[CRR]
  )

class BackupFilesConfigurator[CBR, CRR](
    parent:                       MutableConfigurator[List[Path], _, List[Path], List[Path], _, List[Path]],
    override val initialSettings: Settings
) extends MutableConfigurator[List[Path], List[Path], CBR, List[Path], List[Path], CRR](Some(parent), initialSettings) {
  self: MutableConfigurator[List[Path], List[Path], CBR, List[Path], List[Path], CRR] =>

  implicit val withSettings: Settings = settingsProxy

  val files: mutable.MutableList[String] = mutable.MutableList()

  override def taskBuilder = new BackupFiles
}
