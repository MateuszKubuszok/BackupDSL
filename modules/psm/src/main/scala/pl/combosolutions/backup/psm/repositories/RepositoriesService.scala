package pl.combosolutions.backup.psm.repositories

import pl.combosolutions.backup.psm.PsmExceptionMessages.NoRepositoriesAvailable
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesService
import pl.combosolutions.backup.{ AsyncResult, Logging, ReportException }

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

trait RepositoriesServiceComponentImpl extends RepositoriesServiceComponent with Logging {

  override lazy val repositoriesService = List(
    // Linux repositories
    AptRepositoriesService
  ) find (_.repositoriesAvailable) getOrElse (ReportException onIllegalStateOf NoRepositoriesAvailable)
}
