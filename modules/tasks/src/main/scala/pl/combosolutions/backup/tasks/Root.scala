package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ Async, ExecutionContexts, Reporting }
import ExecutionContexts.Task.context

object Root extends Reporting {

  type Any2Result = Traversable[Any] => Async[Unit]

  private val unitResult: Async[Unit] = Async {
    reporter inform "All task executed"
    Some(Unit)
  }

  private def backupAction(implicit withSettings: Settings): Any2Result = _ => unitResult

  private def restoreAction(implicit withSettings: Settings): Any2Result = _ => unitResult

  class BackupSubTaskBuilder(implicit withSettings: Settings)
    extends ChildDependentSubTaskBuilder[Unit, Unit, Any](backupAction)

  class RestoreSubTaskBuilder(implicit withSettings: Settings)
    extends ChildDependentSubTaskBuilder[Unit, Unit, Any](restoreAction)
}

import Root._

class Root(implicit withSettings: Settings) extends TaskBuilder[Unit, Unit, Any, Unit, Unit, Any](
  new BackupSubTaskBuilder,
  new RestoreSubTaskBuilder
)

class RootConfigurator(
    override val initialSettings: Settings
) extends Configurator[Unit, Unit, Any, Unit, Unit, Any](None, initialSettings) {

  implicit val withSettings = initialSettings

  override val builder = new Root
}
