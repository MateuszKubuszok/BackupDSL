package pl.combosolutions.backup.dsl.internals.operations.windows

import pl.combosolutions.backup.dsl.internals.operations.{PlatformSpecificElevation, Program}
import pl.combosolutions.backup.dsl.internals.{OperatingSystem, Windows95System, Windows98System, WindowsMESystem}

object EmptyElevation extends PlatformSpecificElevation {

  override val elevationAvailable: Boolean =
    Set[OperatingSystem](Windows95System, Windows98System, WindowsMESystem) contains OperatingSystem.current

  override def elevate[T <: Program[T]](program: Program[T]): Program[T] = program
}

object UACElevation extends PlatformSpecificElevation {

  override val elevationAvailable: Boolean = OperatingSystem.current.isWindows && !EmptyElevation.elevationAvailable

  override def elevate[T <: Program[T]](program: Program[T]): Program[T] = throw new NotImplementedError("TODO")
}
