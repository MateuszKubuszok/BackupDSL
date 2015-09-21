package pl.combosolutions.backup.psm.elevation.windows

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.ElevationService
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

object EmptyElevationService extends ElevationService with ComponentsHelper {
  this: ElevationService with OperatingSystemComponent =>

  override val elevationAvailable: Boolean =
    Set[OperatingSystem](Windows95System, Windows98System, WindowsMESystem) contains operatingSystem

  override val elevationCMD: String = ""

  override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = program

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] = program
}

object UACElevationService extends ElevationService with ComponentsHelper {
  this: ElevationService with OperatingSystemComponent =>

  override val elevationAvailable: Boolean = operatingSystem.isWindows && !EmptyElevationService.elevationAvailable

  override val elevationCMD: String = ""

  override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = ReportException onToDoCodeIn getClass

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] =
    ReportException onToDoCodeIn getClass
}
