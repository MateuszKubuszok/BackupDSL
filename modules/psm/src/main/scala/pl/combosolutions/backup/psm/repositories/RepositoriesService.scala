package pl.combosolutions.backup.psm.repositories

import pl.combosolutions.backup.Async
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoRepositoriesAvailable
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent

import RepositoriesServiceComponentImpl.resolve

trait RepositoriesService {

  val repositoriesAvailable: Boolean

  // format: OFF
  type Repositories = List[Repository]
  def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[Repositories]
  def addRepositories(repositories: Repositories)
                     (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean]
  def removeRepositories(repositories: Repositories)
                        (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean]
  def updateRepositories(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean]

  type Packages = List[Package]
  def areAllInstalled(packages: Packages)(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[Boolean]
  def installAll(packages: Packages)
                (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean]
  // format: ON
}

trait RepositoriesServiceComponent {

  def repositoriesService: RepositoriesService
}

object RepositoriesServiceComponentImpl extends ImplementationResolver[RepositoriesService] {

  override lazy val implementations = Seq(
    // Linux repositories
    AptRepositoriesServiceComponent.repositoriesService
  )

  override lazy val notFoundMessage = NoRepositoriesAvailable

  override def byFilter(service: RepositoriesService): Boolean = service.repositoriesAvailable

  // TODO: improve
  override def byPriority(service: RepositoriesService): ImplementationPriority =
    if (service.repositoriesAvailable) Allowed
    else NotAllowed
}

trait RepositoriesServiceComponentImpl extends RepositoriesServiceComponent {

  override lazy val repositoriesService = resolve
}
