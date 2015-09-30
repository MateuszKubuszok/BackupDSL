package pl.combosolutions.backup.psm.repositories

import pl.combosolutions.backup.psm.PsmExceptionMessages.NoRepositoriesAvailable
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent
import pl.combosolutions.backup.{ AsyncResult, ReportException }

import RepositoriesServiceComponentImpl._

trait RepositoriesService {

  val repositoriesAvailable: Boolean

  // format: OFF
  type Repositories = List[Repository]
  def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Repositories]
  def addRepositories(repositories: Repositories)
                     (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
  def removeRepositories(repositories: Repositories)
                        (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]

  type Packages = List[Package]
  def areAllInstalled(packages: Packages)(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
  def installAll(packages: Packages)
                (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
  // format: ON
}

trait RepositoriesServiceComponent {

  def repositoriesService: RepositoriesService
}

object RepositoriesServiceComponentImpl {

  lazy val implementations = Seq(
    // Linux repositories
    AptRepositoriesServiceComponent.repositoriesService
  )
}

trait RepositoriesServiceComponentImpl extends RepositoriesServiceComponent {

  override lazy val repositoriesService = implementations.
    find(_.repositoriesAvailable).
    getOrElse(ReportException onIllegalStateOf NoRepositoriesAvailable)
}
