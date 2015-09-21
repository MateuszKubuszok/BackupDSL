package pl.combosolutions.backup.psm.operations

import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.elevation.{ObligatoryElevationMode, ElevationMode}
import pl.combosolutions.backup.psm.repositories.{Repository, Package}

trait PlatformSpecificRepositories {

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
