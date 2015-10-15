package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ Async, AsyncTransformer, ReportException }
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context
import pl.combosolutions.backup.tasks.Action.{ Backup, Restore }
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

final class Task[BR, PBR, CBR, RR, PRR, CRR](
    private[tasks] val backupSubTask:  SubTask[BR],
    private[tasks] val restoreSubTask: SubTask[RR]
) extends TType[BR, PBR, CBR, RR, PRR, CRR] {

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
