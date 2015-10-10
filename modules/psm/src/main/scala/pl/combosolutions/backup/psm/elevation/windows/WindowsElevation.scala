package pl.combosolutions.backup.psm.elevation.windows

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.elevation.{ ElevationFacadeComponent, ElevationFacadeComponentImpl, ElevationService, ElevationServiceComponent }
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

trait EmptyElevationServiceComponent extends ElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with OperatingSystemComponent =>

  override def elevationService: ElevationService = EmptyElevationService

  trait EmptyElevationService extends ElevationService {

    override val elevationAvailable: Boolean =
      Set[OperatingSystem](Windows95System, Windows98System, WindowsMESystem) contains operatingSystem

    override val elevationCMD: String = ""

    override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = program

    override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] = program
  }

  object EmptyElevationService extends EmptyElevationService
}

object EmptyElevationServiceComponent
  extends EmptyElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl

trait UACElevationServiceComponent extends ElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with OperatingSystemComponent =>

  override def elevationService: ElevationService = UACElevationService

  // TODO:
  // use http://code.kliu.org/misc/elevate/ downloaded with
  // http://stackoverflow.com/questions/27466869/download-a-zip-from-url-and-extract-it-in-resource-using-sbt
  trait UACElevationService extends ElevationService {

    override val elevationAvailable: Boolean =
      operatingSystem.isWindows && !EmptyElevationServiceComponent.elevationService.elevationAvailable

    override val elevationCMD: String = ""

    override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = ReportException onToDoCodeIn getClass

    override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] =
      ReportException onToDoCodeIn getClass
  }

  object UACElevationService extends UACElevationService
}

object UACElevationServiceComponent
  extends UACElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
