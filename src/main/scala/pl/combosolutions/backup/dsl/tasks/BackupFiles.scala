package pl.combosolutions.backup.dsl.tasks

import java.nio.file.{Files, Paths, Path}

import pl.combosolutions.backup.dsl.{Settings, Task}
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success,Try}

import BackupFiles._

object BackupFiles {
  type BackupResult  = List[Path]
  type RestoreResult = List[Path]
}

class BackupFiles[PBR,PRR](files: List[Path]) extends Task[PBR,PRR,BackupResult,RestoreResult] {

  override protected def backup(parentResult: PBR)
                               (implicit settings: Settings): AsyncResult[BackupResult] =
    combineSubResults ( paths => Files.copy(paths._2, paths._3, settings.copyOptions:_*) )

  override protected def restore(parentResult: PRR)
                                (implicit settings: Settings): AsyncResult[RestoreResult] =
    combineSubResults ( paths => Files.copy(paths._3, paths._2, settings.copyOptions:_*) )

  private def combineSubResults(copyAction: ((Path, Path, Path)) => Path)
                               (implicit settings: Settings) =
    Future sequence (hashPaths map { paths =>
      Future {
        Try { copyAction(paths) } match {
          case Success(path) => Some(path)
          case _ => None
        }
      }
    }) map (list => Some(list collect { case Some(path) => path }))

  private def hashPaths(implicit settings: Settings) = files map { file =>
    val backup  = file.toAbsolutePath
    val restore = Paths.get(settings.backupDir.toString, "files", backup.hashCode.toString)
    (file, backup, restore)
  }
}
