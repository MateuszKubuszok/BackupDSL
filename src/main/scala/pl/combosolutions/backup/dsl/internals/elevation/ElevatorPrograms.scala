package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.internals.DefaultsAndConsts.exceptionRemoteKilling
import pl.combosolutions.backup.dsl.internals.ExecutionContexts.Program.context
import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecific
import pl.combosolutions.backup.dsl.internals.programs.Program

import scala.concurrent.Future

import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class DirectElevatorProgram[T <: Program[T]](
  program: Program[T]) extends Program[T](
  PlatformSpecific.current.elevationCMD,
  PlatformSpecific.current.elevationArgs ++ (program.name :: program.arguments)
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

  override def run2Kill = throw new NotImplementedError(exceptionRemoteKilling)
}
