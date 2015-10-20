package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.Cleaner
import pl.combosolutions.backup.psm
import psm.ImplementationPriority._
import psm.ImplementationResolver
import psm.PsmExceptionMessages.NoElevationAvailable
import psm.commands.Command
import psm.elevation.posix.SudoElevationServiceComponent
import psm.elevation.posix.linux.{ GKSudoElevationServiceComponent, KDESudoElevationServiceComponent }
import psm.elevation.windows.{ EmptyElevationServiceComponent, UACElevationServiceComponent }
import psm.programs.Program

import ElevationServiceComponentImpl.resolve

trait ElevationService {

  val elevationAvailable: Boolean

  val elevationPriority: ImplementationPriority

  val elevationCMD: String

  val elevationArgs: List[String] = List()

  def elevateDirect[T <: Program[T]](program: Program[T]): Program[T]

  def elevateRemote[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T]

  def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}

trait ElevationServiceComponent {

  def elevationService: ElevationService
}

// $COVERAGE-OFF$ Implementation resolution should be checked on each implementation level
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

  override def byPriority(service: ElevationService): ImplementationPriority = service.elevationPriority
}

trait ElevationServiceComponentImpl extends ElevationServiceComponent {

  override lazy val elevationService = resolve
}
// $COVERAGE-ON$
