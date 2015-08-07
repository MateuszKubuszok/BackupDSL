package pl.combosolutions.backup.dsl.internals.operations.posix

import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.internals.operations.{Program, PlatformSpecificElevation}

object SudoElevation extends PlatformSpecificElevation {

  override val elevationAvailable: Boolean = OperatingSystem.current.isPosix

  override def elevate[T <: Program[T]](program: Program[T]): Program[T] = throw new NotImplementedError("TODO")
}
