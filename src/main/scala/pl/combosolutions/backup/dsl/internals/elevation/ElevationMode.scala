package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.internals.operations.{Cleaner, PlatformSpecific}
import pl.combosolutions.backup.dsl.internals.programs.Program

sealed trait ElevationMode {
  def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

object NotElevated extends ElevationMode {
  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) = program
}

sealed trait ObligatoryElevationMode extends ElevationMode

object DirectElevation extends ObligatoryElevationMode {
  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    PlatformSpecific.current.elevateDirect(program)
}

object RemoteElevation extends ObligatoryElevationMode {
  override def apply[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    PlatformSpecific.current.elevateRemote(program, cleaner)
}
