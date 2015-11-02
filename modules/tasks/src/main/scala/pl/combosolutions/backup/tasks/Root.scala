package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ Async, ExecutionContexts, Reporting }
import ExecutionContexts.Task.context

object Root extends Reporting {

  type Any2Result = Traversable[Any] => Async[Unit]

  private val unitResult: Async[Unit] = Async {
    reporter inform "All task executed"
    Some(Unit)
  }

  private def backupAction: Any2Result = _ => unitResult

  private def restoreAction: Any2Result = _ => unitResult

  class BackupSubTaskBuilder extends ChildDependentSubTaskBuilder[Unit, Unit, Any](backupAction)

  class RestoreSubTaskBuilder extends ChildDependentSubTaskBuilder[Unit, Unit, Any](restoreAction)
}

import Root._

class Root extends TaskBuilder[Unit, Unit, Any, Unit, Unit, Any](
  new BackupSubTaskBuilder,
  new RestoreSubTaskBuilder
)

// $COVERAGE-OFF$ Hard to test, no real benefit
class RootConfigurator extends Configurator[Unit, Unit, Any, Unit, Unit, Any](None) {

  override val builder = new Root
}
// $COVERAGE-ON$
