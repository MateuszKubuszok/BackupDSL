package pl.combosolutions.backup.psm.elevation.windows

import pl.combosolutions.backup.{ Cleaner, ReportException }
import pl.combosolutions.backup.psm
import psm.ImplementationPriority.{ ImplementationPriority, NotAllowed, OnlyAllowed }
import psm.commands.Command
import psm.elevation._
import psm.programs.Program
import psm.systems._

trait EmptyElevationServiceComponent extends ElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with OperatingSystemComponent =>

  override def elevationService: ElevationService = EmptyElevationService

  trait EmptyElevationService extends ElevationService {

    override val elevationAvailable: Boolean =
      Set[OperatingSystem](Windows95System, Windows98System, WindowsMESystem) contains operatingSystem

    override val elevationPriority: ImplementationPriority = if (elevationAvailable) OnlyAllowed else NotAllowed

    override val elevationCMD: String = ""

    override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = program

    override def elevateRemote[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T] = command

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

    override val elevationPriority: ImplementationPriority = if (elevationAvailable) OnlyAllowed else NotAllowed

    override val elevationCMD: String = ""

    override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] = ReportException onToDoCodeIn getClass

    override def elevateRemote[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T] =
      ReportException onToDoCodeIn getClass

    override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] =
      ReportException onToDoCodeIn getClass
  }

  object UACElevationService extends UACElevationService
}

object UACElevationServiceComponent
  extends UACElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
