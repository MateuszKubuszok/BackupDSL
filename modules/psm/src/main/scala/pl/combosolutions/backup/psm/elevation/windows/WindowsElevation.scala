package pl.combosolutions.backup.psm.elevation.windows

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.{OperatingSystem, Windows95System, Windows98System, WindowsMESystem}
import pl.combosolutions.backup.psm.operations.{Cleaner, PlatformSpecificElevation}
import pl.combosolutions.backup.psm.programs.Program

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
