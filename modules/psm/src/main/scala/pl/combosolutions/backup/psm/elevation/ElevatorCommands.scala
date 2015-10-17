package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.ExecutionContexts.Command.context
import pl.combosolutions.backup.psm.commands.Command

final case class RemoteElevatorCommand[T <: Command[T]](
    command:         Command[T],
    elevationFacade: ElevationFacade
) extends Command[T] {

  override def run: Async[Result[T]] = (elevationFacade runRemotely command).asAsync map (_.asSpecific[T])
}
