package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.{ ExecutionContexts, ReportException }
import ExecutionContexts.Program.context
import pl.combosolutions.backup.psm.PsmExceptionMessages.{ RemoteGeneric, RemoteKilling }
import pl.combosolutions.backup.psm.programs.Program

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

  override def run = (elevationFacade runRemotely program.asGeneric).asAsync map (_.asSpecific[T])

  override def run2Kill = ReportException onNotImplemented RemoteKilling

  override def asGeneric = ReportException onNotImplemented RemoteGeneric
}
