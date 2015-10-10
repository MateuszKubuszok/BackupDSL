package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoElevationAvailable
import pl.combosolutions.backup.psm.commands.Command
import pl.combosolutions.backup.psm.elevation.posix.SudoElevationServiceComponent
import pl.combosolutions.backup.psm.elevation.posix.linux.{ GKSudoElevationServiceComponent, KDESudoElevationServiceComponent }
import pl.combosolutions.backup.psm.elevation.windows.{ EmptyElevationServiceComponent, UACElevationServiceComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

import ElevationServiceComponentImpl.resolve

trait ElevationService {

  val elevationAvailable: Boolean

  val elevationCMD: String

  val elevationArgs: List[String] = List()

  def elevateDirect[T <: Program[T]](program: Program[T]): Program[T]

  def elevateRemote[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T]

  def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

trait ElevationServiceComponent {

  def elevationService: ElevationService
}

object ElevationServiceComponentImpl extends ImplementationResolver[ElevationService] {

  override lazy val implementations = Seq(
    // Windows elevation
    EmptyElevationServiceComponent.elevationService,
    UACElevationServiceComponent.elevationService,

    // Linux elevation
    GKSudoElevationServiceComponent.elevationService,
    KDESudoElevationServiceComponent.elevationService,

    // POSIX elevation
    SudoElevationServiceComponent.elevationService
  )

  override lazy val notFoundMessage = NoElevationAvailable

  override def byFilter(service: ElevationService): Boolean = service.elevationAvailable

  // TODO: improve
  override def byPriority(service: ElevationService): ImplementationPriority =
    if (service.elevationAvailable) Allowed
    else NotAllowed
}

trait ElevationServiceComponentImpl extends ElevationServiceComponent {

  override lazy val elevationService = resolve
}
