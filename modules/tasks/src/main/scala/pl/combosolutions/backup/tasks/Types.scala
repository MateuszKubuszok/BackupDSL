package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.tasks

trait SType[R, PR, CR] {

  type Result = R
  type ParentResult = PR
  type ChildResult = CR

  type ParentDependentSubTaskT = ParentDependentSubTask[Result, ParentResult]
  type ChildDependentSubTaskT = ChildDependentSubTask[Result, ChildResult]

  type SubTaskT = SubTask[Result]
  type SubTaskBuilderT = SubTaskBuilder[Result, ParentResult, ChildResult]
}

trait TType[BR, PBR, CBR, RR, PRR, CRR] {

  type BackupResult = BR
  type ParentBackupResult = PBR
  type ChildBackupResult = CBR
  type RestoreResult = RR
  type ParentRestoreResult = PRR
  type ChildRestoreResult = CRR

  type BackupSubTaskBuilderT = SubTaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult]
  type RestoreSubTaskBuilderT = SubTaskBuilder[RestoreResult, ParentRestoreResult, ChildRestoreResult]

  type TaskT = tasks.Task[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]
  type TaskBuilderT = tasks.TaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]
  type TaskConfigT = tasks.TaskConfig[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]
}
