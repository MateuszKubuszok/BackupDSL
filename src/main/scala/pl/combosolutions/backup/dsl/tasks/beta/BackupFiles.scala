package pl.combosolutions.backup.dsl.tasks.beta

import java.nio.file.{ Files, Paths, Path }

import pl.combosolutions.backup.dsl.tasks.beta.BackupFiles.{ RestoreSubTaskBuilder, BackupSubTaskBuilder }
import pl.combosolutions.backup.dsl.{ AsyncResult, Settings }
import pl.combosolutions.backup.dsl.internals.ExecutionContexts.Task.context

import scala.util.{ Success, Try }

object BackupFiles {

  private def backupPassedFilesAction(implicit withSettings: Settings): (List[Path]) => AsyncResult[List[Path]] =
    (files: List[Path]) =>
      combineSubResults(files) { paths =>
        print(s"Backing up ${paths._2} into ${paths._3}... ")
        paths._3.toFile.mkdirs
        Files.copy(paths._2, paths._3, withSettings.copyOptions: _*)
      }

  private def restorePassedFilesAction(implicit withSettings: Settings): (List[Path]) => AsyncResult[List[Path]] =
    (files: List[Path]) =>
      combineSubResults(files) { paths =>
        print(s"Restoring ${paths._3} into ${paths._2}... ")
        paths._2.toFile.mkdirs
        Files.copy(paths._3, paths._2, withSettings.copyOptions: _*)
      }

  private def combineSubResults(files: List[Path])(copyAction: ((Path, Path, Path)) => Path)(implicit withSettings: Settings): AsyncResult[List[Path]] =
    AsyncResult incompleteSequence {
      hashPaths(files) map { paths =>
        AsyncResult {
          Try {
            copyAction(paths)
          } match {
            case Success(path) =>
              println("success")
              Some(path)
            case _ =>
              println("failure")
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
