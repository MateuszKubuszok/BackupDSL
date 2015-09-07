package pl.combosolutions.backup.dsl.internals.operations

import java.nio.file.Path

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.DefaultsAndConsts._
import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.internals.elevation.{ObligatoryElevationMode, ElevationMode}
import pl.combosolutions.backup.dsl.internals.elevation.posix.SudoElevation
import pl.combosolutions.backup.dsl.internals.elevation.posix.linux.{KDESudoElevation, GKSudoElevation}
import pl.combosolutions.backup.dsl.internals.elevation.windows.{UACElevation, EmptyElevation}
import pl.combosolutions.backup.dsl.internals.filesystem.posix.PosixFileSystem
import pl.combosolutions.backup.dsl.internals.programs.Program
import Program._
import pl.combosolutions.backup.dsl.internals.repositories.posix.linux.AptRepositories

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
    extends PlatformSpecific with Logging {

  logger trace s"Operating System -> ${OperatingSystem.current.name}"
  logger trace s"Elevation        -> ${elevationPS.getClass.getSimpleName}"
  logger trace s"File System      -> ${fileSystemPS.getClass.getSimpleName}"
  logger trace s"Repositories     -> ${repositoriesPS.getClass.getSimpleName}"

  // elevation

  override val elevationAvailable = elevationPS.elevationAvailable
  override val elevationCMD       = elevationPS.elevationCMD
  override val elevationArgs      = elevationPS.elevationArgs

  override def elevateDirect[T <: Program[T]](program: Program[T]) = elevationPS elevateDirect program
  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationPS elevateRemote(program, cleaner)

  // file system

  override val fileSystemAvailable  = fileSystemPS.fileSystemAvailable
  override val fileIsFile           = fileSystemPS.fileIsFile
  override val fileIsDirectory      = fileSystemPS.fileIsDirectory
  override val fileIsSymlinkPattern = fileSystemPS.fileIsSymlinkPattern

  override def getFileType(forPath: Path)
                          (implicit withElevation: ElevationMode, cleaner: Cleaner) = fileSystemPS getFileType forPath

  // repositories

  override val repositoriesAvailable = repositoriesPS.repositoriesAvailable

  override def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Repositories] =
    repositoriesPS obtainRepositories
  override def addRepositories(repositories: Repositories)
                              (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS addRepositories repositories
  override def removeRepositories(repositories: Repositories)
                                 (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS removeRepositories repositories

  override def areAllInstalled(packages: Packages)(implicit withElevation: ElevationMode, cleaner: Cleaner) =
    repositoriesPS areAllInstalled packages
  override def installAll(packages: Packages)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS installAll packages
}
