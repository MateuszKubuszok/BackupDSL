package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.Async
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

object RootBuilder {

  private val unitResult: Async[Unit] = Async { Some(Unit) }

  private def backupAction(implicit withSettings: Settings): () => Async[Unit] = () => unitResult

  private def restoreAction(implicit withSettings: Settings): () => Async[Unit] = () => unitResult

  class BackupSubTaskBuilder(implicit withSettings: Settings)
    extends IndependentSubTaskBuilder[Unit, Unit, Any](backupAction)

  class RestoreSubTaskBuilder(implicit withSettings: Settings)
    extends IndependentSubTaskBuilder[Unit, Unit, Any](restoreAction)
}

import RootBuilder._

class RootBuilder(implicit withSettings: Settings) extends TaskBuilder[Unit, Unit, Any, Unit, Unit, Any](
  new BackupSubTaskBuilder,
  new RestoreSubTaskBuilder
)

class RootConfigurator(
    override val initialSettings: Settings
) extends MutableConfigurator[Unit, Unit, Any, Unit, Unit, Any](None, initialSettings) {
  self: MutableConfigurator[Unit, Unit, Any, Unit, Unit, Any] =>

  implicit val withSettings: Settings = settingsProxy

  override def taskBuilder = new RootBuilder
}
