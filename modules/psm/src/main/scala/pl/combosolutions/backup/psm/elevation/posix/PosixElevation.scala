package pl.combosolutions.backup.psm.elevation.posix

import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.{ DirectElevatorProgram, ElevationFacadeComponentImpl, ElevationService, RemoteElevatorProgram }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.programs.posix.PosixPrograms._
import pl.combosolutions.backup.psm.programs.posix.WhichProgram
import pl.combosolutions.backup.psm.systems.OperatingSystemComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CommonElevationService extends ElevationService with ElevationFacadeComponentImpl {

  override lazy val elevationAvailable: Boolean =
    Await.result(WhichProgram(elevationCMD).digest[Boolean], Duration.Inf) getOrElse false

  override def elevateDirect[T <: Program[T]](program: Program[T]) =
    DirectElevatorProgram[T](program, this)

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    RemoteElevatorProgram[T](program, elevationFacadeFor(cleaner))
}

object SudoElevationService extends CommonElevationService with ComponentsHelper {
  this: CommonElevationService with OperatingSystemComponent =>

  override lazy val elevationAvailable: Boolean = operatingSystem.isPosix

  override val elevationCMD: String = "sudo"
}
