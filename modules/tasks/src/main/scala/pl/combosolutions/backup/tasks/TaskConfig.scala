package pl.combosolutions.backup.tasks

case class TaskConfig[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult](
    parent:   Option[TaskBuilder[ParentBackupResult, _, _, ParentRestoreResult, _, _]]    = None,
    children: Traversable[TaskBuilder[ChildBackupResult, _, _, ChildRestoreResult, _, _]] = Seq()
) {

  def setParent(parent: TaskBuilder[ParentBackupResult, _, _, ParentRestoreResult, _, _]) = this.copy(parent = Some(parent))

  def addChild(child: TaskBuilder[ChildBackupResult, _, _, ChildRestoreResult, _, _]) = this.copy(children = children ++ Seq(child))
}
