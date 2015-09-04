package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals.programs.Program
import Program._
import pl.combosolutions.backup.dsl.internals.repositories.{Package, Repository}

trait PlatformSpecificRepositories {
  val repositoriesAvailable: Boolean

  type Repositories = List[Repository]
  def obtainRepositories: AsyncResult[Repositories]
  def addRepositories(repositories: Repositories): AsyncResult[Boolean]
  def removeRepositories(repositories: Repositories): AsyncResult[Boolean]

  type Packages = List[Package]
  def areAllInstalled(packages: Packages): AsyncResult[Boolean]
  def installAll(packages: Packages): AsyncResult[Boolean]

  // TODO: withElevation: ElevationMode/ObligatoryElevationMode
}
