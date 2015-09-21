package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.ExecutionContexts.Program.context
import pl.combosolutions.backup.psm.PsmExceptionMessages.RemoteKilling
import pl.combosolutions.backup.psm.programs.Program

import scala.concurrent.Future

import scalaz.OptionT._
import scalaz.std.scalaFuture._

object ElevatorProgram extends ComponentsHelper {
  this: ElevationServiceComponent =>

  def elevationCMD = elevationService.elevationCMD

  def elevationArgs = elevationService.elevationArgs
}

case class DirectElevatorProgram[T <: Program[T]](
  program: Program[T]) extends Program[T](
  ElevatorProgram.elevationCMD,
  ElevatorProgram.elevationArgs ++ (program.name :: program.arguments)
)

case class RemoteElevatorProgram[T <: Program[T]](
  program: Program[T],
  elevationFacade: ElevationFacade) extends Program[T](
  program.name,
  program.arguments
) {

  override def run = (for {
    result <- optionT[Future](elevationFacade runRemotely program.asGeneric)
  } yield result.asSpecific[T]).run

  override def run2Kill = ReportException onNotImplemented RemoteKilling
}
