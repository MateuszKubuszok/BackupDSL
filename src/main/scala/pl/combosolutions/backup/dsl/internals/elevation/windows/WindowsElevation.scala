package pl.combosolutions.backup.dsl.internals.elevation.windows

import pl.combosolutions.backup.dsl.ReportException
import pl.combosolutions.backup.dsl.internals.operations.{ Cleaner, PlatformSpecificElevation }
import pl.combosolutions.backup.dsl.internals.programs.Program
import pl.combosolutions.backup.dsl.internals.{ OperatingSystem, Windows95System, Windows98System, WindowsMESystem }

object EmptyElevation extends PlatformSpecificElevation {

  override val elevationAvailable: Boolean =
    Set[OperatingSystem](Windows95System, Windows98System, WindowsMESystem) contains OperatingSystem.current

  override val elevationCMD: String = ""

  override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = program

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] = program
}

object UACElevation extends PlatformSpecificElevation {

  override val elevationAvailable: Boolean = OperatingSystem.current.isWindows && !EmptyElevation.elevationAvailable

  override val elevationCMD: String = ""

  override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = ReportException onToDoCodeIn getClass

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] =
    ReportException onToDoCodeIn getClass
}
