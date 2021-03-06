package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ ExecutionContexts, Async, AsyncTransformer, ReportException }
import ExecutionContexts.Task.context
import pl.combosolutions.backup.tasks.Action.{ Backup, Restore }
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

final class Task[BackupResult, RestoreResult](
    private[tasks] val backupSubTask:  SubTask[BackupResult],
    private[tasks] val restoreSubTask: SubTask[RestoreResult]
) {

  private lazy val backupResult = backupSubTask.result
  private lazy val restoreResult = restoreSubTask.result

  def backup: Async[BackupResult] = backupResult

  def restore: Async[RestoreResult] = restoreResult

  def eitherResult(action: Action.Value): Async[Either[RestoreResult, BackupResult]] = action match {
    case Backup  => backupResult.asAsync map (Right(_))
    case Restore => restoreResult.asAsync map (Left(_))
    case _       => ReportException onIllegalStateOf InvalidScriptAction
  }
}
