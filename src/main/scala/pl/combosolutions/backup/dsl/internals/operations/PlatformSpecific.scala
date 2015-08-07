package pl.combosolutions.backup.dsl.internals.operations

import java.nio.file.Path

import pl.combosolutions.backup.dsl.internals.DefaultsAndConsts._
import pl.combosolutions.backup.dsl.internals.filesystem.FileType._
import pl.combosolutions.backup.dsl.internals.operations.Program._
import pl.combosolutions.backup.dsl.internals.operations.posix.{SudoElevation, PosixFileSystem}
import pl.combosolutions.backup.dsl.internals.operations.posix.linux.{AptRepositories, KDESudoElevation, GKSudoElevation}
import pl.combosolutions.backup.dsl.internals.operations.windows.{UACElevation, EmptyElevation}

import scala.util.matching.Regex

object PlatformSpecific {
  lazy val current: PlatformSpecific = new CalculatedPlatformSpecific(
    currentElevation,
    currentFileSystem,
    currentRepositories
  )

  private lazy val currentElevation = List(
    // Windows elevation
    EmptyElevation,
    UACElevation,

    // Linux elevation
    GKSudoElevation,
    KDESudoElevation,

    // POSIX elevation
    SudoElevation
  ) find (_.elevationAvailable) getOrElse (throw new IllegalStateException(exceptionNoElevation))

  private lazy val currentFileSystem = List(
    // POSIX file system
    PosixFileSystem
  ) find (_.fileSystemAvailable) getOrElse (throw new IllegalStateException(exceptionNoFileSystem))

  private lazy val currentRepositories = List(
    // Linux repositories
    AptRepositories
  ) find (_.repositoriesAvailable) getOrElse (throw new IllegalStateException(exceptionNoRepositories))
}

trait PlatformSpecific
  extends PlatformSpecificElevation
  with PlatformSpecificFileSystem
  with PlatformSpecificRepositories

class CalculatedPlatformSpecific(
    elevationPS: PlatformSpecificElevation,
    fileSystemPS: PlatformSpecificFileSystem,
    repositoriesPS: PlatformSpecificRepositories)
    extends PlatformSpecific {

  // elevation

  override val elevationAvailable = elevationPS.elevationAvailable

  override def elevate[T <: Program[T]](program: Program[T]): Program[T] = elevationPS.elevate(program)

  // file system

  override val fileSystemAvailable: Boolean = fileSystemPS.fileSystemAvailable

  override val fileIsFile: Regex = fileSystemPS.fileIsFile
  override val fileIsDirectory: Regex = fileSystemPS.fileIsDirectory
  override val fileIsSymlinkPattern: Regex = fileSystemPS.fileIsSymlinkPattern
  override def getFileType(path: Path): AsyncResult[FileType] = fileSystemPS.getFileType(path)

  // repositories

  override val repositoriesAvailable: Boolean = repositoriesPS.repositoriesAvailable

  override def obtainRepositories: AsyncResult[Repositories] = repositoriesPS.obtainRepositories
  override def addRepositories(repositories: Repositories): AsyncResult[Boolean] = repositoriesPS.addRepositories(repositories)
  override def removeRepositories(repositories: Repositories): AsyncResult[Boolean]= repositoriesPS.removeRepositories(repositories)

  override def areAllInstalled(packages: Packages): AsyncResult[Boolean] = repositoriesPS.areAllInstalled(packages)
  override def installAll(packages: Packages): AsyncResult[Boolean] = repositoriesPS.installAll(packages)
}
