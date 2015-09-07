package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals.elevation.{ObligatoryElevationMode, ElevationMode}
import pl.combosolutions.backup.dsl.internals.programs.Program
import Program._
import pl.combosolutions.backup.dsl.internals.repositories.{Package, Repository}

trait PlatformSpecificRepositories {
  val repositoriesAvailable: Boolean

  type Repositories = List[Repository]
  def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Repositories]
  def addRepositories(repositories: Repositories)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
  def removeRepositories(repositories: Repositories)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]

  type Packages = List[Package]
  def areAllInstalled(packages: Packages)(implicit  withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
  def installAll(packages: Packages)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): AsyncResult[Boolean]
}
