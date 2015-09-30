package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoElevationAvailable
import pl.combosolutions.backup.psm.elevation.posix.SudoElevationServiceComponent
import pl.combosolutions.backup.psm.elevation.posix.linux.{ GKSudoElevationServiceComponent, KDESudoElevationServiceComponent }
import pl.combosolutions.backup.psm.elevation.windows.{ EmptyElevationServiceComponent, UACElevationServiceComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

import ElevationServiceComponentImpl._

trait ElevationService {

  val elevationAvailable: Boolean

  val elevationCMD: String

  val elevationArgs: List[String] = List()

  def elevateDirect[T <: Program[T]](program: Program[T]): Program[T]

  def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

trait ElevationServiceComponent {

  def elevationService: ElevationService
}

object ElevationServiceComponentImpl {

  lazy val implementations = Seq(
    // Windows elevation
    EmptyElevationServiceComponent.elevationService,
    UACElevationServiceComponent.elevationService,

    // Linux elevation
    GKSudoElevationServiceComponent.elevationService,
    KDESudoElevationServiceComponent.elevationService,

    // POSIX elevation
    SudoElevationServiceComponent.elevationService
  )
}

trait ElevationServiceComponentImpl extends ElevationServiceComponent {

  override lazy val elevationService = implementations.
    find(_.elevationAvailable).
    getOrElse(ReportException onIllegalStateOf NoElevationAvailable)
}
