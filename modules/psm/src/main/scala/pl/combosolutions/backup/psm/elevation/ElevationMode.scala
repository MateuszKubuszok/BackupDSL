package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

sealed trait ElevationMode {

  def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

object NotElevated extends ElevationMode {

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) = program
}

sealed trait ObligatoryElevationMode extends ElevationMode

object DirectElevation extends ObligatoryElevationMode with ComponentsHelper {
  this: ObligatoryElevationMode with ElevationServiceComponent =>

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationService elevateDirect program
}

object RemoteElevation extends ObligatoryElevationMode with ComponentsHelper {
  this: ObligatoryElevationMode with ElevationServiceComponent =>

  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationService elevateRemote (program, cleaner)
}

class ElevateIfNeeded[T <: Program[T]](program: Program[T], withElevation: ElevationMode, cleaner: Cleaner) {

  def handleElevation = withElevation(program, cleaner)
}

object ElevateIfNeeded {

  // format: OFF
  implicit def possiblyElevated[T <: Program[T]](program: Program[T])
                                                (implicit withElevation: ElevationMode, cleaner: Cleaner) =
    new ElevateIfNeeded(program, withElevation, cleaner)
  // format: ON
}
