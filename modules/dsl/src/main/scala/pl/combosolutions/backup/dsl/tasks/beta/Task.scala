package pl.combosolutions.backup.dsl.tasks.beta

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.dsl.tasks.beta.TasksExceptionMessages.{ InvalidScriptAction, ParentDependentWithoutParent }
import pl.combosolutions.backup.dsl.Action

final class Task[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult](
    private[beta] val backupSubTask: SubTask[BackupResult],
    private[beta] val restoreSubTask: SubTask[RestoreResult]) {

  private lazy val backupResult = backupSubTask.result
  private lazy val restoreResult = restoreSubTask.result

  def backup = backupSubTask.result

  def restore = restoreSubTask.result

  def eitherSubTask(action: Action.Value) = action match {
    case Action.Backup  => Right(() => backupResult)
    case Action.Restore => Left(() => restoreResult)
    case _              => ReportException onIllegalStateOf InvalidScriptAction
  }
}

class TaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult](
    private[beta] val backupSubTaskBuilder: SubTaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult],
    private[beta] val restoreSubTaskBuilder: SubTaskBuilder[RestoreResult, ParentRestoreResult, ChildRestoreResult]) {

  private val task = new Task[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult](backupSubTaskBuilder.injectableProxy, restoreSubTaskBuilder.injectableProxy)

  type Config = TaskConfig[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]

  def build(taskConfig: Config): Task[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult] = {

    val parent = taskConfig.parent
    val children = taskConfig.children

    backupSubTaskBuilder.injectableProxy.dependencyType match {
      case DependencyType.ParentDependent =>
        val parentTask = parent getOrElse ReportException.onIllegalStateOf(ParentDependentWithoutParent)
        backupSubTaskBuilder configureForParent parentTask.backupSubTaskBuilder
      case DependencyType.ChildDependent =>
        backupSubTaskBuilder configureForChildren (children map (_.backupSubTaskBuilder))
    }

    restoreSubTaskBuilder.injectableProxy.dependencyType match {
      case DependencyType.ParentDependent =>
        val parentTask = parent getOrElse ReportException.onIllegalStateOf(ParentDependentWithoutParent)
        restoreSubTaskBuilder configureForParent parentTask.restoreSubTaskBuilder
      case DependencyType.ChildDependent =>
        restoreSubTaskBuilder configureForChildren (children map (_.restoreSubTaskBuilder))
    }

    task
  }
}
