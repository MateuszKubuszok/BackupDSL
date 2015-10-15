package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ExecutionContexts.Program.context
import pl.combosolutions.backup.psm.PsmExceptionMessages.{ RemoteGeneric, RemoteKilling }
import pl.combosolutions.backup.psm.programs.Program

import scala.concurrent.Future

import scalaz.OptionT._
import scalaz.std.scalaFuture._

final case class DirectElevatorProgram[T <: Program[T]](
  program: Program[T], elevationService: ElevationService
) extends Program[T](
  elevationService.elevationCMD,
  elevationService.elevationArgs ++ (program.name :: program.arguments)
)

final case class RemoteElevatorProgram[T <: Program[T]](
  program:         Program[T],
  elevationFacade: ElevationFacade
) extends Program[T](
  program.name,
  program.arguments
) {

  override def run = (for {
    result <- optionT[Future](elevationFacade runRemotely program.asGeneric)
  } yield result.asSpecific[T]).run

  override def run2Kill = ReportException onNotImplemented RemoteKilling

  override def asGeneric = ReportException onNotImplemented RemoteGeneric
}
