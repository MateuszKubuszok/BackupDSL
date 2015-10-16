package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.{ Cleaner, ReportException }
import pl.combosolutions.backup.psm.PsmExceptionMessages.DirectCommand
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.commands.Command
import pl.combosolutions.backup.psm.programs.Program

sealed trait ElevationMode {

  def apply[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T]

  def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

private[elevation] trait NotElevated extends ElevationMode {

  override def apply[T <: Command[T]](command: Command[T], cleaner: Cleaner) = command

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) = program
}
object NotElevated extends NotElevated

sealed trait ObligatoryElevationMode extends ElevationMode

private[elevation] trait DirectElevation extends ObligatoryElevationMode with ComponentsHelper {
  this: ObligatoryElevationMode with ElevationServiceComponent =>

  override def apply[T <: Command[T]](command: Command[T], cleaner: Cleaner) =
    ReportException onIllegalStateOf DirectCommand

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationService elevateDirect program
}
object DirectElevation extends DirectElevation

private[elevation] trait RemoteElevation extends ObligatoryElevationMode with ComponentsHelper {
  this: ObligatoryElevationMode with ElevationServiceComponent =>

  override def apply[T <: Command[T]](command: Command[T], cleaner: Cleaner) =
    elevationService elevateRemote (command, cleaner)

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationService elevateRemote (program, cleaner)
}
object RemoteElevation extends RemoteElevation

private[elevation] class ElevateCommandIfNeeded[T <: Command[T]](
    command:       Command[T],
    withElevation: ElevationMode,
    cleaner:       Cleaner
) {

  def handleElevation = withElevation(command, cleaner)
}

private[elevation] class ElevateProgramIfNeeded[T <: Program[T]](
    program:       Program[T],
    withElevation: ElevationMode,
    cleaner:       Cleaner
) {

  def handleElevation = withElevation(program, cleaner)
}

object ElevateIfNeeded {

  // format: OFF
  implicit def possiblyElevated[T <: Command[T]](command: Command[T])
                                                (implicit withElevation: ElevationMode, cleaner: Cleaner) =
    new ElevateCommandIfNeeded(command, withElevation, cleaner)

  implicit def possiblyElevated[T <: Program[T]](program: Program[T])
                                                (implicit withElevation: ElevationMode, cleaner: Cleaner) =
    new ElevateProgramIfNeeded(program, withElevation, cleaner)
  // format: ON
}
