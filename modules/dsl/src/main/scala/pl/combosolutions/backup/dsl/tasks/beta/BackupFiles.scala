package pl.combosolutions.backup.dsl.tasks.beta

import java.nio.file.{ Files, Path, Paths }

import pl.combosolutions.backup.{ Async, Reporting }
import pl.combosolutions.backup.dsl.tasks.beta.BackupFiles.{ BackupSubTaskBuilder, RestoreSubTaskBuilder }
import pl.combosolutions.backup.dsl.Settings
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

import scala.util.{ Failure, Success, Try }

object BackupFiles extends Reporting {

  private def backupPassedFilesAction(implicit withSettings: Settings): (List[Path]) => Async[List[Path]] =
    (files: List[Path]) =>
      combineSubResults(files) { paths =>
        reporter details s"Backing up ${paths._2} into ${paths._3}... "
        paths._3.toFile.mkdirs
        Files.copy(paths._2, paths._3, withSettings.copyOptions: _*) // TODO: compress into TAR archive and allow elevation
      }

  private def restorePassedFilesAction(implicit withSettings: Settings): (List[Path]) => Async[List[Path]] =
    (files: List[Path]) =>
      combineSubResults(files) { paths =>
        reporter details s"Restoring ${paths._3} into ${paths._2}... "
        paths._2.toFile.mkdirs
        Files.copy(paths._3, paths._2, withSettings.copyOptions: _*) // TODO: compress into TAR archive and allow elevation
      }

  private def combineSubResults(files: List[Path])(copyAction: ((Path, Path, Path)) => Path)(implicit withSettings: Settings): Async[List[Path]] =
    Async incompleteSequence {
      hashPaths(files) map { paths =>
        Async {
          Try {
            copyAction(paths)
          } match {
            case Success(path) =>
              reporter inform s"Copied up $path successfully"
              Some(path)
            case Failure(ex) =>
              reporter error (s"Failed to copy file", ex)
              None
          }
        }
      }
    }

  private def hashPaths(files: List[Path])(implicit withSettings: Settings) = files map { file =>
    val backup = file.toAbsolutePath
    val restore = Paths.get(withSettings.backupDir.toString, "files", backup.hashCode.toString)
    (file, backup, restore)
  }

  class BackupSubTaskBuilder[ChildResult](withSettings: Settings)
    extends ParentDependentSubTaskBuilder[List[Path], List[Path], ChildResult](backupPassedFilesAction(withSettings))

  class RestoreSubTaskBuilder[ChildResult](withSettings: Settings)
    extends ParentDependentSubTaskBuilder[List[Path], List[Path], ChildResult](restorePassedFilesAction(withSettings))
}

class BackupFiles[ChildBackupResult, ChildRestoreResult](withSettings: Settings)
  extends TaskBuilder[List[Path], List[Path], ChildBackupResult, List[Path], List[Path], ChildRestoreResult](
    new BackupSubTaskBuilder[ChildBackupResult](withSettings),
    new RestoreSubTaskBuilder[ChildRestoreResult](withSettings))
