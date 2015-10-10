package pl.combosolutions.backup.psm.commands

import pl.combosolutions.backup.{ Async, Result }

trait Command[T <: Command[T]] extends Serializable {

  def execute(): Async[Result[T]]
}
