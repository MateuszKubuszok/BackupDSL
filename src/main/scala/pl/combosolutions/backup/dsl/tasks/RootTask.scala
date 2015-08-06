package pl.combosolutions.backup.dsl.tasks

import pl.combosolutions.backup.dsl.{Task, Settings}

import scala.concurrent.Future

class RootTask extends Task[Unit,Unit,Unit,Unit] {

  def performBackup(implicit withSettings: Settings): Unit = performBackupWithResult(Unit)

  def performRestore(implicit withSettings: Settings): Unit = performRestoreWithResult(Unit)

  override protected def backup(parentResult: Unit)(implicit withSettings: Settings) = Future successful Some(Unit)

  override protected def restore(parentResult: Unit)(implicit withSettings: Settings) = Future successful Some(Unit)
}
