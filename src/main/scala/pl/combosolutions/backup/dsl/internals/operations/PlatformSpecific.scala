package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals._
import pl.combosolutions.backup.dsl.internals.filesystem.FSPath
import pl.combosolutions.backup.dsl.internals.filesystem.FileType.FileType
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.posix.linux.DebianOperations
import pl.combosolutions.backup.dsl.internals.repositories.{Package, Repository}

import scala.util.matching.Regex

object PlatformSpecific {
  lazy val current: PlatformSpecific = OperatingSystem.current match {
    case DebianSystem => DebianOperations // TODO: implement for the rest of the world when needed
  }
}

abstract trait PlatformSpecific {
  abstract def elevate[T <: Program](program: Program[T]): Program[T]

  val fileIsFile: Regex
  val fileIsDirectory: Regex
  val fileIsSymlinkPattern: Regex
  abstract def getFileType(fSPath: FSPath): AsyncResult[FileType]

  type Repositories <: Traversable[Repository]
  abstract def obtainRepositories: AsyncResult[Repositories]
  abstract def addRepositories(repositories: Repositories): AsyncResult[Boolean]
  abstract def removeRepositories(repositories: Repositories): AsyncResult[Boolean]

  type Packages <: Traversable[Package]
  abstract def areAllInstalled(packages: Packages): AsyncResult[Boolean]
  abstract def installAll(packages: Packages): AsyncResult[Boolean]
}
