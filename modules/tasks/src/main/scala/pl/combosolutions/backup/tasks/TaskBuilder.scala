package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ tasks, ReportException }
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

class TaskBuilder[BR, PBR, CBR, RR, PRR, CRR](
    private[tasks] val backupSubTaskBuilder:  SubTaskBuilder[BR, PBR, CBR],
    private[tasks] val restoreSubTaskBuilder: SubTaskBuilder[RR, PRR, CRR]
) {

  type BackupResult = BR
  type ParentBackupResult = PBR
  type ChildBackupResult = CBR
  type RestoreResult = RR
  type ParentRestoreResult = PRR
  type ChildRestoreResult = CRR

  type BackupSubTaskT = SubTask[BR]
  type BackupSubTaskBuilderT = SubTaskBuilder[BR, PBR, CBR]

  type RestoreSubTaskT = SubTask[RR]
  type RestoreSubTaskBuilderT = SubTaskBuilder[RR, PRR, CRR]

  type TaskT = tasks.Task[BackupResult, RestoreResult]
  type TaskBuilderT = tasks.TaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]
  type TaskConfigT = tasks.TaskConfig[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]

  type ParentTaskBuilderT = TaskBuilder[ParentBackupResult, _, BackupResult, ParentRestoreResult, _, RestoreResult]

  type ChildTaskBuilderT = TaskBuilder[ChildBackupResult, BackupResult, _, ChildRestoreResult, RestoreResult, _]

  private val task = new TaskT(backupSubTaskBuilder.injectableProxy, restoreSubTaskBuilder.injectableProxy)

  def buildFor(taskConfig: TaskConfigT): TaskT = {

    def parent = taskConfig.parent getOrElse (ReportException onIllegalStateOf ParentDependentWithoutParent)
    def children = taskConfig.children

    backupSubTaskBuilder.injectableProxy.dependencyType match {
      case ParentDependent => backupSubTaskBuilder configureForParent parent.backupSubTaskBuilder
      case ChildDependent  => backupSubTaskBuilder configureForChildren (children map (_.backupSubTaskBuilder))
      case _ =>
    }

    restoreSubTaskBuilder.injectableProxy.dependencyType match {
      case ParentDependent => restoreSubTaskBuilder configureForParent parent.restoreSubTaskBuilder
      case ChildDependent  => restoreSubTaskBuilder configureForChildren (children map (_.restoreSubTaskBuilder))
      case _ =>
    }

    task
  }

  def newConfig: TaskConfigT = new TaskConfigT
}
