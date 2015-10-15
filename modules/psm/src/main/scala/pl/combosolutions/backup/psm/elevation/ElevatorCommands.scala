package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.psm.ExecutionContexts.Command.context
import pl.combosolutions.backup.psm.commands.Command

import scala.concurrent.Future

import scalaz.OptionT._
import scalaz.std.scalaFuture._

final case class RemoteElevatorCommand[T <: Command[T]](
    command:         Command[T],
    elevationFacade: ElevationFacade
) extends Command[T] {

  override def run = (for {
    result <- optionT[Future](elevationFacade runRemotely command)
  } yield result.asSpecific[T]).run
}
