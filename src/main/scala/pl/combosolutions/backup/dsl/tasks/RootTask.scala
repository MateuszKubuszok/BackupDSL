package pl.combosolutions.backup.dsl.tasks

import pl.combosolutions.backup.dsl.{Task, Settings}

import scala.concurrent.Future

object RootTask extends Task[Unit,Unit,Unit,Unit] {

  def performBackup(implicit settings: Settings): Unit = performBackupWithResult(Unit)

  def performRestore(implicit settings: Settings): Unit = performBackupWithResult(Unit)

  override protected def backup(parentResult: Unit)(implicit settings: Settings) = Future successful Some(Unit)

  override protected def restore(parentResult: Unit)(implicit settings: Settings) = Future successful Some(Unit)
}
