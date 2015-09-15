package pl.combosolutions.backup.dsl.tasks.beta

abstract class Task[ParentBackupResult, BackupResult, ParentRestoreResult, RestoreResult](
    backupSubTask: SubTask[BackupResult],
    restoreSubTask: SubTask[RestoreResult]) {

}

