package pl.combosolutions.backup.dsl.tasks

import java.nio.file.{ Files, Paths, Path }

import pl.combosolutions.backup.dsl.Settings
import pl.combosolutions.backup.dsl.internals.programs.Program
import Program.AsyncResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Success, Try }

import BackupFiles._

object BackupFiles {
  type BackupResult = List[String]
  type RestoreResult = List[String]
}

case class BackupFiles[PBR, PRR](files: List[String]) extends Task[PBR, PRR, BackupResult, RestoreResult]("backup files") {

  override protected def backup(parentResult: PBR)(implicit withSettings: Settings): AsyncResult[BackupResult] =
    combineSubResults { paths =>
      print(s"Backing up ${paths._2} into ${paths._3}... ")
      paths._3.toFile.mkdirs
      Files.copy(paths._2, paths._3, withSettings.copyOptions: _*)
    }

  override protected def restore(parentResult: PRR)(implicit withSettings: Settings): AsyncResult[RestoreResult] =
    combineSubResults { paths =>
      print(s"Restoring ${paths._3} into ${paths._2}... ")
      paths._2.toFile.mkdirs
      Files.copy(paths._3, paths._2, withSettings.copyOptions: _*)
    }

  private def combineSubResults(copyAction: ((String, Path, Path)) => Path)(implicit withSettings: Settings) =
    Future sequence (hashPaths map { paths =>
      Future {
        Try { copyAction(paths) } match {
          case Success(path) =>
            println("success")
            Some(path)
          case _ =>
            println("failure")
            None
        }
      }
    }) map (list => Some(list collect { case Some(path) => path.toString }))

  private def hashPaths(implicit withSettings: Settings) = files map { file =>
    val backup = Paths get file toAbsolutePath
    val restore = Paths.get(withSettings.backupDir.toString, "files", backup.hashCode.toString)
    (file, backup, restore)
  }
}
