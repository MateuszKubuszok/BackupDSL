package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.psm.PsmExceptionMessages.NoElevationAvailable
import pl.combosolutions.backup.psm.elevation.posix.SudoElevationService
import pl.combosolutions.backup.psm.elevation.posix.linux.{ GKSudoElevationService, KDESudoElevationService }
import pl.combosolutions.backup.psm.elevation.windows.{ EmptyElevationService, UACElevationService }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.{ Logging, ReportException }

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

trait ElevationServiceComponentImpl extends ElevationServiceComponent with Logging {

  override lazy val elevationService = Seq(
    // Windows elevation
    EmptyElevationService,
    UACElevationService,

    // Linux elevation
    GKSudoElevationService,
    KDESudoElevationService,

    // POSIX elevation
    SudoElevationService
  ) find (_.elevationAvailable) getOrElse (ReportException onIllegalStateOf NoElevationAvailable)
}
